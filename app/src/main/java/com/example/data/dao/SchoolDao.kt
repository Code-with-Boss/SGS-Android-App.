package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FeeRecord
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentFund
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    // --- Classes ---
    @Query("SELECT * FROM classes ORDER BY displayOrder ASC, id ASC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Query("SELECT * FROM classes WHERE id = :id")
    fun getClassById(id: Long): Flow<SchoolClass?>

    @Query("SELECT * FROM classes WHERE id = :id")
    suspend fun getClassByIdDirect(id: Long): SchoolClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<SchoolClass>): List<Long>

    @Update
    suspend fun updateClass(schoolClass: SchoolClass)

    @Query("DELETE FROM classes WHERE id = :id")
    suspend fun deleteClassById(id: Long)

    // --- Students ---
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY rollNo ASC")
    fun getStudentsByClass(classId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY rollNo ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentByIdDirect(id: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>): List<Long>

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("SELECT COUNT(*) FROM students WHERE classId = :classId")
    fun countStudentsByClass(classId: Long): Flow<Int>

    // --- Fee Records ---
    @Query("SELECT * FROM fee_records WHERE studentId = :studentId AND year = :year ORDER BY month ASC")
    fun getFeeRecordsForStudent(studentId: Long, year: Int): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records WHERE studentId = :studentId AND year = :year ORDER BY month ASC")
    suspend fun getFeeRecordsForStudentDirect(studentId: Long, year: Int): List<FeeRecord>

    @Query("SELECT * FROM fee_records WHERE year = :year")
    fun getAllFeeRecordsForYear(year: Int): Flow<List<FeeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFeeRecord(feeRecord: FeeRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRecords(feeRecords: List<FeeRecord>): List<Long>

    @Query("SELECT * FROM fee_records WHERE studentId = :studentId AND year = :year AND month = :month LIMIT 1")
    suspend fun getFeeRecord(studentId: Long, year: Int, month: Int): FeeRecord?

    // --- Student Funds ---
    @Query("SELECT * FROM student_funds WHERE studentId = :studentId ORDER BY year DESC, month ASC")
    fun getFundsForStudent(studentId: Long): Flow<List<StudentFund>>

    @Query("SELECT * FROM student_funds WHERE studentId = :studentId")
    suspend fun getFundsForStudentDirect(studentId: Long): List<StudentFund>

    @Query("SELECT * FROM student_funds WHERE year = :year")
    fun getAllFundsForYear(year: Int): Flow<List<StudentFund>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: StudentFund): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFunds(funds: List<StudentFund>): List<Long>

    @Update
    suspend fun updateFund(fund: StudentFund)

    @Query("DELETE FROM student_funds WHERE id = :id")
    suspend fun deleteFundById(id: Long)
}
