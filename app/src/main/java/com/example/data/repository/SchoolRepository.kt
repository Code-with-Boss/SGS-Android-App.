package com.example.data.repository

import com.example.data.dao.SchoolDao
import com.example.data.model.ClassWithStats
import com.example.data.model.FeeRecord
import com.example.data.model.SchoolClass
import com.example.data.model.SchoolOverviewStats
import com.example.data.model.Student
import com.example.data.model.StudentFeeSummary
import com.example.data.model.StudentFund
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SchoolRepository(private val dao: SchoolDao) {

    val allClasses: Flow<List<SchoolClass>> = dao.getAllClasses()
    val allStudents: Flow<List<Student>> = dao.getAllStudents()

    fun getClassById(id: Long): Flow<SchoolClass?> = dao.getClassById(id)

    suspend fun getClassByIdDirect(id: Long): SchoolClass? = dao.getClassByIdDirect(id)

    fun getClassesWithStats(year: Int = 2026): Flow<List<ClassWithStats>> {
        return combine(
            dao.getAllClasses(),
            dao.getAllStudents(),
            dao.getAllFeeRecordsForYear(year),
            dao.getAllFundsForYear(year)
        ) { classes, students, feeRecords, funds ->
            val studentsByClass = students.groupBy { it.classId }
            val feesByStudent = feeRecords.groupBy { it.studentId }
            val fundsByStudent = funds.groupBy { it.studentId }

            classes.map { schoolClass ->
                val classStudents = studentsByClass[schoolClass.id].orEmpty()
                var collected = 0.0
                var totalExpected = 0.0

                classStudents.forEach { student ->
                    val monthlyFee = student.customMonthlyFee ?: schoolClass.monthlyFee
                    totalExpected += monthlyFee * 12

                    val studentFees = feesByStudent[student.id].orEmpty()
                    collected += studentFees.sumOf { it.paidAmount }

                    val studentFunds = fundsByStudent[student.id].orEmpty()
                    totalExpected += studentFunds.sumOf { it.amount }
                    collected += studentFunds.filter { it.isPaid }.sumOf { it.amount }
                }

                val outstanding = (totalExpected - collected).coerceAtLeast(0.0)

                ClassWithStats(
                    schoolClass = schoolClass,
                    studentCount = classStudents.size,
                    totalCollected = collected,
                    totalOutstanding = outstanding
                )
            }
        }
    }

    fun getOverviewStats(year: Int = 2026): Flow<SchoolOverviewStats> {
        return combine(
            dao.getAllClasses(),
            dao.getAllStudents(),
            dao.getAllFeeRecordsForYear(year),
            dao.getAllFundsForYear(year)
        ) { classes, students, feeRecords, funds ->
            val classMap = classes.associateBy { it.id }
            var monthlyExpected = 0.0
            var annualExpected = 0.0
            var totalCollected = 0.0

            students.forEach { student ->
                val classFee = classMap[student.classId]?.monthlyFee ?: 0.0
                val fee = student.customMonthlyFee ?: classFee
                monthlyExpected += fee
                annualExpected += fee * 12
            }

            totalCollected += feeRecords.sumOf { it.paidAmount }

            funds.forEach { fund ->
                annualExpected += fund.amount
                if (fund.isPaid) {
                    totalCollected += fund.amount
                }
            }

            val totalOutstanding = (annualExpected - totalCollected).coerceAtLeast(0.0)

            SchoolOverviewStats(
                totalClasses = classes.size,
                totalStudents = students.size,
                totalExpectedMonthly = monthlyExpected,
                totalCollectedYear = totalCollected,
                totalOutstandingYear = totalOutstanding
            )
        }
    }

    fun getStudentsWithSummaryForClass(
        classId: Long,
        year: Int = 2026,
        currentMonth: Int = 9
    ): Flow<List<StudentFeeSummary>> {
        return combine(
            dao.getClassById(classId),
            dao.getStudentsByClass(classId),
            dao.getAllFeeRecordsForYear(year),
            dao.getAllFundsForYear(year)
        ) { schoolClass, students, allFeeRecords, allFunds ->
            val feeRecordsByStudent = allFeeRecords.groupBy { it.studentId }
            val fundsByStudent = allFunds.groupBy { it.studentId }

            students.map { student ->
                val records = feeRecordsByStudent[student.id].orEmpty().associateBy { it.month }
                val funds = fundsByStudent[student.id].orEmpty()

                val standardFee = student.customMonthlyFee ?: (schoolClass?.monthlyFee ?: 0.0)

                var totalPaid = 0.0
                var totalExpectedYear = standardFee * 12
                var totalExpectedUpToMonth = standardFee * currentMonth

                // Add monthly paid
                for (m in 1..12) {
                    val record = records[m]
                    if (record != null) {
                        totalPaid += record.paidAmount
                    }
                }

                // Add funds
                funds.forEach { fund ->
                    totalExpectedYear += fund.amount
                    if (fund.month in 1..currentMonth) {
                        totalExpectedUpToMonth += fund.amount
                    }
                    if (fund.isPaid) {
                        totalPaid += fund.amount
                    }
                }

                val totalOutstandingYear = (totalExpectedYear - totalPaid).coerceAtLeast(0.0)
                val totalArrearsUpToMonth = (totalExpectedUpToMonth - totalPaid).coerceAtLeast(0.0)

                StudentFeeSummary(
                    student = student,
                    schoolClass = schoolClass,
                    monthlyRecords = records,
                    funds = funds,
                    totalPaid = totalPaid,
                    totalOutstandingYear = totalOutstandingYear,
                    totalArrearsUpToMonth = totalArrearsUpToMonth
                )
            }
        }
    }

    fun getStudentFeeSummary(
        studentId: Long,
        year: Int = 2026,
        currentMonth: Int = 9
    ): Flow<StudentFeeSummary?> {
        return combine(
            dao.getStudentById(studentId),
            dao.getAllClasses(),
            dao.getFeeRecordsForStudent(studentId, year),
            dao.getFundsForStudent(studentId)
        ) { student, classes, feeRecords, funds ->
            if (student == null) return@combine null

            val schoolClass = classes.find { it.id == student.classId }
            val records = feeRecords.associateBy { it.month }

            val standardFee = student.customMonthlyFee ?: (schoolClass?.monthlyFee ?: 0.0)

            var totalPaid = 0.0
            var totalExpectedYear = standardFee * 12
            var totalExpectedUpToMonth = standardFee * currentMonth

            for (m in 1..12) {
                val record = records[m]
                if (record != null) {
                    totalPaid += record.paidAmount
                }
            }

            funds.forEach { fund ->
                totalExpectedYear += fund.amount
                if (fund.month in 1..currentMonth) {
                    totalExpectedUpToMonth += fund.amount
                }
                if (fund.isPaid) {
                    totalPaid += fund.amount
                }
            }

            val totalOutstandingYear = (totalExpectedYear - totalPaid).coerceAtLeast(0.0)
            val totalArrearsUpToMonth = (totalExpectedUpToMonth - totalPaid).coerceAtLeast(0.0)

            StudentFeeSummary(
                student = student,
                schoolClass = schoolClass,
                monthlyRecords = records,
                funds = funds,
                totalPaid = totalPaid,
                totalOutstandingYear = totalOutstandingYear,
                totalArrearsUpToMonth = totalArrearsUpToMonth
            )
        }
    }

    // --- Actions ---
    suspend fun insertClass(name: String, monthlyFee: Double): Long {
        val nextOrder = (dao.getClassByIdDirect(1)?.displayOrder ?: 0) + 1
        return dao.insertClass(
            SchoolClass(
                name = name,
                monthlyFee = monthlyFee,
                displayOrder = nextOrder
            )
        )
    }

    suspend fun updateClass(schoolClass: SchoolClass) {
        dao.updateClass(schoolClass)
    }

    suspend fun deleteClassById(id: Long) {
        dao.deleteClassById(id)
    }

    suspend fun insertStudent(
        classId: Long,
        rollNo: String,
        name: String,
        fatherName: String,
        contact: String,
        customMonthlyFee: Double?,
        admissionDate: String = "2026-01-10",
        notes: String = ""
    ): Long {
        val student = Student(
            classId = classId,
            rollNo = rollNo,
            name = name,
            fatherName = fatherName,
            contact = contact,
            customMonthlyFee = customMonthlyFee,
            admissionDate = admissionDate,
            notes = notes
        )
        val studentId = dao.insertStudent(student)

        // Initialize 12 months fee records for this student
        val cls = dao.getClassByIdDirect(classId)
        val expectedFee = customMonthlyFee ?: (cls?.monthlyFee ?: 2000.0)
        val feeRecords = (1..12).map { month ->
            FeeRecord(
                studentId = studentId,
                year = 2026,
                month = month,
                expectedFee = expectedFee,
                paidAmount = 0.0,
                status = "UNPAID"
            )
        }
        dao.insertFeeRecords(feeRecords)

        return studentId
    }

    suspend fun updateStudent(student: Student) {
        dao.updateStudent(student)
    }

    suspend fun deleteStudentById(id: Long) {
        dao.deleteStudentById(id)
    }

    suspend fun recordFeePayment(
        studentId: Long,
        year: Int,
        month: Int,
        paidAmount: Double,
        expectedFee: Double,
        paymentDate: String,
        discount: Double = 0.0,
        paymentMode: String = "Cash",
        receiptNo: String? = null
    ) {
        val status = when {
            paidAmount >= (expectedFee - discount) -> "PAID"
            paidAmount > 0 -> "PARTIAL"
            else -> "UNPAID"
        }

        val existing = dao.getFeeRecord(studentId, year, month)
        val record = FeeRecord(
            id = existing?.id ?: 0,
            studentId = studentId,
            year = year,
            month = month,
            expectedFee = expectedFee,
            paidAmount = paidAmount,
            paymentDate = if (paidAmount > 0) paymentDate else null,
            status = status,
            discount = discount,
            paymentMode = paymentMode,
            receiptNo = receiptNo ?: "REC-$year-$month-$studentId"
        )
        dao.insertOrUpdateFeeRecord(record)
    }

    suspend fun addStudentFund(
        studentId: Long,
        title: String,
        amount: Double,
        month: Int = 0,
        year: Int = 2026,
        isPaid: Boolean = false,
        paidDate: String? = null
    ): Long {
        return dao.insertFund(
            StudentFund(
                studentId = studentId,
                title = title,
                amount = amount,
                month = month,
                year = year,
                isPaid = isPaid,
                paidDate = paidDate
            )
        )
    }

    suspend fun updateFund(fund: StudentFund) {
        dao.updateFund(fund)
    }

    suspend fun deleteFundById(id: Long) {
        dao.deleteFundById(id)
    }
}
