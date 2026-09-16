package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val monthlyFee: Double,
    val displayOrder: Int = 0
)

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId"]), Index(value = ["rollNo"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val rollNo: String,
    val name: String,
    val fatherName: String,
    val contact: String,
    val customMonthlyFee: Double? = null,
    val admissionDate: String = "2026-01-10",
    val notes: String = ""
)

@Entity(
    tableName = "fee_records",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["studentId", "year", "month"], unique = true)
    ]
)
data class FeeRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val year: Int,
    val month: Int, // 1 to 12
    val expectedFee: Double,
    val paidAmount: Double,
    val paymentDate: String? = null,
    val status: String = "UNPAID", // PAID, PARTIAL, UNPAID
    val discount: Double = 0.0,
    val receiptNo: String? = null,
    val paymentMode: String = "Cash"
)

@Entity(
    tableName = "student_funds",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class StudentFund(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val title: String,
    val amount: Double,
    val month: Int, // e.g. month it is billed or 0 for annual
    val year: Int,
    val isPaid: Boolean = false,
    val paidDate: String? = null
)
