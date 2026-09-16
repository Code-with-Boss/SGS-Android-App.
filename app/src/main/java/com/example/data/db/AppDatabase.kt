package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.SchoolDao
import com.example.data.model.FeeRecord
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentFund
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SchoolClass::class, Student::class, FeeRecord::class, StudentFund::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scholar_grammar_db"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.schoolDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: SchoolDao) {
            val classDefinitions = listOf(
                Pair("Playgroup A", 2000.0),
                Pair("Playgroup B", 2000.0),
                Pair("Nursery A", 2200.0),
                Pair("Nursery B", 2200.0),
                Pair("Prep A", 2400.0),
                Pair("Prep B", 2400.0),
                Pair("Class 1 A", 2500.0),
                Pair("Class 1 B", 2500.0),
                Pair("Class 2 A", 2600.0),
                Pair("Class 2 B", 2600.0),
                Pair("Class 3 A", 2800.0),
                Pair("Class 3 B", 2800.0),
                Pair("Class 4 A", 3000.0),
                Pair("Class 4 B", 3000.0),
                Pair("Class 5 A", 3200.0),
                Pair("Class 5 B", 3200.0),
                Pair("Class 6 A", 3500.0),
                Pair("Class 6 B", 3500.0),
                Pair("Class 7 A", 3800.0),
                Pair("Class 7 B", 3800.0),
                Pair("Class 8 A", 4000.0),
                Pair("Class 8 B", 4000.0),
                Pair("Class 9 A", 4500.0),
                Pair("Class 9 B", 4500.0),
                Pair("Class 10 A", 5000.0),
                Pair("Class 10 B", 5000.0)
            )

            val classIds = mutableListOf<Long>()
            classDefinitions.forEachIndexed { index, pair ->
                val id = dao.insertClass(
                    SchoolClass(
                        name = pair.first,
                        monthlyFee = pair.second,
                        displayOrder = index + 1
                    )
                )
                classIds.add(id)
            }

            // Seed sample students across some classes
            val studentNames = listOf(
                Triple("Muhammad Ali", "Tariq Mahmood", "0300-4567891"),
                Triple("Fatima Noor", "Muhammad Asif", "0321-8765432"),
                Triple("Hamza Tariq", "Tariq Aziz", "0333-5432187"),
                Triple("Zainab Bibi", "Abdul Rehman", "0345-6789012"),
                Triple("Bilal Ahmed", "Nadeem Akhtar", "0301-2345678"),
                Triple("Ayesha Khan", "Kamran Khan", "0312-3456789"),
                Triple("Hassan Raza", "Ghulam Raza", "0334-7654321"),
                Triple("Maryam Siddiqui", "Zahid Siddiqui", "0322-1122334"),
                Triple("Usman Ghani", "Muhammad Ghani", "0308-9988776"),
                Triple("Zoya Malik", "Imran Malik", "0346-4455667"),
                Triple("Abdullah Shah", "Syed Anwar Shah", "0303-3344556"),
                Triple("Hoorain Zahra", "Mohsin Ali", "0315-7788990"),
                Triple("Saad Rafique", "Rafique Ahmed", "0323-5566778"),
                Triple("Anaya Fatima", "Sajid Hussain", "0335-9900112"),
                Triple("Mustafa Kamal", "Kamal Uddin", "0306-1212123")
            )

            var rollCounter = 101
            val currentYear = 2026

            // Populate some students in Class 1 A (index 6), Playgroup A (index 0), Class 5 A (index 14), Class 10 A (index 24)
            val targetClasses = listOf(
                0 to studentNames.subList(0, 4),
                2 to studentNames.subList(4, 7),
                6 to studentNames.subList(0, 8),
                14 to studentNames.subList(7, 12),
                24 to studentNames.subList(10, 15)
            )

            for ((clsIdx, sList) in targetClasses) {
                if (clsIdx >= classIds.size) continue
                val cId = classIds[clsIdx]
                val monthlyFee = classDefinitions[clsIdx].second

                sList.forEachIndexed { i, studentData ->
                    val rollNo = "SG-${rollCounter++}"
                    val studentId = dao.insertStudent(
                        Student(
                            classId = cId,
                            rollNo = rollNo,
                            name = studentData.first,
                            fatherName = studentData.second,
                            contact = studentData.third,
                            customMonthlyFee = if (i == 1) monthlyFee - 300.0 else monthlyFee // scholarship for student 1
                        )
                    )

                    val studentFee = if (i == 1) monthlyFee - 300.0 else monthlyFee

                    // Fee records for Jan to Dec
                    val feeRecords = mutableListOf<FeeRecord>()
                    for (month in 1..12) {
                        val record = when {
                            month == 1 -> FeeRecord(
                                studentId = studentId,
                                year = currentYear,
                                month = month,
                                expectedFee = studentFee,
                                paidAmount = studentFee,
                                paymentDate = "2026-01-08",
                                status = "PAID",
                                receiptNo = "REC-01-$studentId"
                            )
                            month == 2 -> if (i % 3 != 0) {
                                FeeRecord(
                                    studentId = studentId,
                                    year = currentYear,
                                    month = month,
                                    expectedFee = studentFee,
                                    paidAmount = studentFee,
                                    paymentDate = "2026-02-10",
                                    status = "PAID",
                                    receiptNo = "REC-02-$studentId"
                                )
                            } else {
                                FeeRecord(
                                    studentId = studentId,
                                    year = currentYear,
                                    month = month,
                                    expectedFee = studentFee,
                                    paidAmount = 0.0,
                                    status = "UNPAID"
                                )
                            }
                            month == 3 -> if (i % 2 == 0) {
                                FeeRecord(
                                    studentId = studentId,
                                    year = currentYear,
                                    month = month,
                                    expectedFee = studentFee,
                                    paidAmount = studentFee,
                                    paymentDate = "2026-03-05",
                                    status = "PAID",
                                    receiptNo = "REC-03-$studentId"
                                )
                            } else {
                                FeeRecord(
                                    studentId = studentId,
                                    year = currentYear,
                                    month = month,
                                    expectedFee = studentFee,
                                    paidAmount = 0.0,
                                    status = "UNPAID"
                                )
                            }
                            else -> FeeRecord(
                                studentId = studentId,
                                year = currentYear,
                                month = month,
                                expectedFee = studentFee,
                                paidAmount = 0.0,
                                status = "UNPAID"
                            )
                        }
                        feeRecords.add(record)
                    }
                    dao.insertFeeRecords(feeRecords)

                    // Add a sample fund item
                    dao.insertFund(
                        StudentFund(
                            studentId = studentId,
                            title = "Exam & Paper Fund",
                            amount = 500.0,
                            month = 3,
                            year = currentYear,
                            isPaid = (i % 2 == 0),
                            paidDate = if (i % 2 == 0) "2026-03-05" else null
                        )
                    )
                }
            }
        }
    }
}
