package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ClassWithStats
import com.example.data.model.FeeRecord
import com.example.data.model.SchoolBranding
import com.example.data.model.SchoolClass
import com.example.data.model.SchoolOverviewStats
import com.example.data.model.Student
import com.example.data.model.StudentFeeSummary
import com.example.data.model.StudentFund
import com.example.data.model.UserRole
import com.example.data.repository.SchoolRepository
import com.example.util.BrandingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = SchoolRepository(database.schoolDao())

    // Role state
    private val _currentUserRole = MutableStateFlow(UserRole.ADMIN)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    // Branding state (Logo, App Icon, School info saved in local storage)
    private val _schoolBranding = MutableStateFlow(BrandingManager.getBranding(application))
    val schoolBranding: StateFlow<SchoolBranding> = _schoolBranding.asStateFlow()

    private val _adminPin = MutableStateFlow("1234")

    // Academic Year
    private val _academicYear = MutableStateFlow(2026)
    val academicYear: StateFlow<Int> = _academicYear.asStateFlow()

    // Current Month (1 to 12)
    private val _currentMonth = MutableStateFlow(9) // September by default
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    // Navigation state
    private val _selectedClassId = MutableStateFlow<Long?>(null)
    val selectedClassId: StateFlow<Long?> = _selectedClassId.asStateFlow()

    private val _selectedStudentId = MutableStateFlow<Long?>(null)
    val selectedStudentId: StateFlow<Long?> = _selectedStudentId.asStateFlow()

    private val _invoiceStudentId = MutableStateFlow<Long?>(null)
    val invoiceStudentId: StateFlow<Long?> = _invoiceStudentId.asStateFlow()

    // Search and filter for student list
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _studentFilter = MutableStateFlow("ALL") // ALL, PAID, DEFAULTER
    val studentFilter: StateFlow<String> = _studentFilter.asStateFlow()

    // Data streams
    val overviewStats: StateFlow<SchoolOverviewStats> = repository.getOverviewStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SchoolOverviewStats(0, 0, 0.0, 0.0, 0.0)
        )

    val classesWithStats: StateFlow<List<ClassWithStats>> = repository.getClassesWithStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedClass: StateFlow<SchoolClass?> = _selectedClassId
        .flatMapLatest { id ->
            if (id != null) repository.getClassById(id) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val classStudents: StateFlow<List<StudentFeeSummary>> = _selectedClassId
        .flatMapLatest { id ->
            if (id != null) repository.getStudentsWithSummaryForClass(id, _academicYear.value, _currentMonth.value)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedStudentSummary: StateFlow<StudentFeeSummary?> = _selectedStudentId
        .flatMapLatest { id ->
            if (id != null) repository.getStudentFeeSummary(id, _academicYear.value, _currentMonth.value)
            else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val invoiceStudentSummary: StateFlow<StudentFeeSummary?> = _invoiceStudentId
        .flatMapLatest { id ->
            if (id != null) repository.getStudentFeeSummary(id, _academicYear.value, _currentMonth.value)
            else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // UI Feedback Message (e.g. snackbar)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Role switching
    fun switchRole(newRole: UserRole, pinInput: String = ""): Boolean {
        if (newRole == UserRole.TEACHER) {
            _currentUserRole.value = UserRole.TEACHER
            showSnackbar("Switched to Teacher Mode (Limited Access)")
            return true
        } else {
            // Switching to Admin requires PIN
            if (pinInput == _adminPin.value || _currentUserRole.value == UserRole.ADMIN) {
                _currentUserRole.value = UserRole.ADMIN
                showSnackbar("Switched to Admin Mode (Full Access)")
                return true
            } else {
                showSnackbar("Invalid Admin PIN! Default is 1234")
                return false
            }
        }
    }

    fun updateAdminPin(newPin: String) {
        if (newPin.isNotBlank()) {
            _adminPin.value = newPin
            showSnackbar("Admin PIN updated successfully")
        }
    }

    // Navigation methods
    fun selectClass(classId: Long?) {
        _selectedClassId.value = classId
        _searchQuery.value = ""
        _studentFilter.value = "ALL"
    }

    fun selectStudent(studentId: Long?) {
        _selectedStudentId.value = studentId
    }

    fun openInvoice(studentId: Long?) {
        _invoiceStudentId.value = studentId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStudentFilter(filter: String) {
        _studentFilter.value = filter
    }

    fun setCurrentMonth(month: Int) {
        _currentMonth.value = month
    }

    // Class Operations (Admin)
    fun addClass(name: String, monthlyFee: Double) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Permission denied: Only Admin can add classes")
            return
        }
        viewModelScope.launch {
            repository.insertClass(name, monthlyFee)
            showSnackbar("Class '$name' added successfully")
        }
    }

    fun updateClass(schoolClass: SchoolClass) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Permission denied: Only Admin can edit classes")
            return
        }
        viewModelScope.launch {
            repository.updateClass(schoolClass)
            showSnackbar("Class '${schoolClass.name}' updated")
        }
    }

    fun deleteClass(classId: Long) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Permission denied: Only Admin can delete classes")
            return
        }
        viewModelScope.launch {
            repository.deleteClassById(classId)
            if (_selectedClassId.value == classId) {
                _selectedClassId.value = null
            }
            showSnackbar("Class deleted")
        }
    }

    // Student Operations
    fun addStudent(
        classId: Long,
        rollNo: String,
        name: String,
        fatherName: String,
        contact: String,
        customMonthlyFee: Double?,
        admissionDate: String = "2026-01-10",
        notes: String = ""
    ) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Permission denied: Only Admin can register students")
            return
        }
        viewModelScope.launch {
            repository.insertStudent(
                classId = classId,
                rollNo = rollNo,
                name = name,
                fatherName = fatherName,
                contact = contact,
                customMonthlyFee = customMonthlyFee,
                admissionDate = admissionDate,
                notes = notes
            )
            showSnackbar("Student '$name' registered successfully")
        }
    }

    fun updateStudent(student: Student) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Permission denied: Only Admin can edit student profile")
            return
        }
        viewModelScope.launch {
            repository.updateStudent(student)
            showSnackbar("Student details updated")
        }
    }

    fun deleteStudent(studentId: Long) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Permission denied: Only Admin can delete students")
            return
        }
        viewModelScope.launch {
            repository.deleteStudentById(studentId)
            if (_selectedStudentId.value == studentId) {
                _selectedStudentId.value = null
            }
            showSnackbar("Student record removed")
        }
    }

    // Fee Payment Operations (Both Teacher and Admin allowed!)
    fun recordFeePayment(
        studentId: Long,
        month: Int,
        paidAmount: Double,
        expectedFee: Double,
        discount: Double = 0.0,
        paymentMode: String = "Cash",
        paymentDate: String? = null
    ) {
        viewModelScope.launch {
            val dateStr = paymentDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.recordFeePayment(
                studentId = studentId,
                year = _academicYear.value,
                month = month,
                paidAmount = paidAmount,
                expectedFee = expectedFee,
                paymentDate = dateStr,
                discount = discount,
                paymentMode = paymentMode
            )
            showSnackbar("Fee payment of Rs. ${paidAmount.toInt()} recorded!")
        }
    }

    // Fund operations
    fun addFund(studentId: Long, title: String, amount: Double, month: Int = 0, isPaid: Boolean = false) {
        viewModelScope.launch {
            val dateStr = if (isPaid) SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) else null
            repository.addStudentFund(
                studentId = studentId,
                title = title,
                amount = amount,
                month = month,
                year = _academicYear.value,
                isPaid = isPaid,
                paidDate = dateStr
            )
            showSnackbar("Fund '$title' added (Rs. ${amount.toInt()})")
        }
    }

    fun updateFund(fund: StudentFund) {
        viewModelScope.launch {
            repository.updateFund(fund)
            showSnackbar("Fund updated")
        }
    }

    fun deleteFund(fundId: Long) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Permission denied: Only Admin can delete funds")
            return
        }
        viewModelScope.launch {
            repository.deleteFundById(fundId)
            showSnackbar("Fund item removed")
        }
    }

    // Branding & Logo Operations (Admin only)
    fun updateSchoolLogo(uri: Uri) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Only Admin can change school logo")
            return
        }
        viewModelScope.launch {
            val saved = BrandingManager.saveImageFromUri(getApplication(), uri, isAppIcon = false)
            if (saved != null) {
                _schoolBranding.value = BrandingManager.getBranding(getApplication())
                showSnackbar("سکول کا نیا لوگو محفوظ ہو گیا (Saved to Local Storage)!")
            } else {
                showSnackbar("Failed to save school logo")
            }
        }
    }

    fun updateAppIcon(uri: Uri) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Only Admin can change app icon")
            return
        }
        viewModelScope.launch {
            val saved = BrandingManager.saveImageFromUri(getApplication(), uri, isAppIcon = true)
            if (saved != null) {
                _schoolBranding.value = BrandingManager.getBranding(getApplication())
                showSnackbar("ایپ کا نیا آئیکون محفوظ ہو گیا (App Icon Saved to Local Storage)!")
            } else {
                showSnackbar("Failed to save app icon")
            }
        }
    }

    fun resetSchoolLogo() {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Only Admin can reset school logo")
            return
        }
        BrandingManager.resetLogo(getApplication(), isAppIcon = false)
        _schoolBranding.value = BrandingManager.getBranding(getApplication())
        showSnackbar("سکول کا اصل لوگو بحال کر دیا گیا (Reset to default)")
    }

    fun resetAppIcon() {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Only Admin can reset app icon")
            return
        }
        BrandingManager.resetLogo(getApplication(), isAppIcon = true)
        _schoolBranding.value = BrandingManager.getBranding(getApplication())
        showSnackbar("ایپ کا اصل آئیکون بحال کر دیا گیا (Reset to default)")
    }

    fun updateSchoolInfo(name: String, tagline: String) {
        if (_currentUserRole.value != UserRole.ADMIN) {
            showSnackbar("Only Admin can update school details")
            return
        }
        BrandingManager.updateSchoolInfo(getApplication(), name, tagline)
        _schoolBranding.value = BrandingManager.getBranding(getApplication())
        showSnackbar("School details updated")
    }
}
