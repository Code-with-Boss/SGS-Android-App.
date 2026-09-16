package com.example.data.model

enum class UserRole {
    ADMIN,
    TEACHER
}

data class StudentFeeSummary(
    val student: Student,
    val schoolClass: SchoolClass?,
    val monthlyRecords: Map<Int, FeeRecord>, // 1 to 12
    val funds: List<StudentFund>,
    val totalPaid: Double,
    val totalOutstandingYear: Double,
    val totalArrearsUpToMonth: Double
)

data class ClassWithStats(
    val schoolClass: SchoolClass,
    val studentCount: Int,
    val totalCollected: Double,
    val totalOutstanding: Double
)

data class SchoolOverviewStats(
    val totalClasses: Int,
    val totalStudents: Int,
    val totalExpectedMonthly: Double,
    val totalCollectedYear: Double,
    val totalOutstandingYear: Double
)

val MonthNames = listOf(
    "January", "February", "March", "April",
    "May", "June", "July", "August",
    "September", "October", "November", "December"
)

val MonthShortNames = listOf(
    "Jan", "Feb", "Mar", "Apr",
    "May", "Jun", "Jul", "Aug",
    "Sep", "Oct", "Nov", "Dec"
)
