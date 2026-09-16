package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.RoleSwitchDialog
import com.example.ui.screens.ClassStudentsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvoiceScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SchoolViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SchoolFeeApp()
            }
        }
    }
}

@Composable
fun SchoolFeeApp(
    viewModel: SchoolViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val overviewStats by viewModel.overviewStats.collectAsStateWithLifecycle()
    val classesWithStats by viewModel.classesWithStats.collectAsStateWithLifecycle()

    val selectedClassId by viewModel.selectedClassId.collectAsStateWithLifecycle()
    val selectedClass by viewModel.selectedClass.collectAsStateWithLifecycle()
    val classStudents by viewModel.classStudents.collectAsStateWithLifecycle()

    val selectedStudentId by viewModel.selectedStudentId.collectAsStateWithLifecycle()
    val selectedStudentSummary by viewModel.selectedStudentSummary.collectAsStateWithLifecycle()

    val invoiceStudentId by viewModel.invoiceStudentId.collectAsStateWithLifecycle()
    val invoiceStudentSummary by viewModel.invoiceStudentSummary.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val studentFilter by viewModel.studentFilter.collectAsStateWithLifecycle()

    val schoolBranding by viewModel.schoolBranding.collectAsStateWithLifecycle()

    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var showRoleSwitchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearSnackbar()
        }
    }

    // Handle Back Button hierarchically
    BackHandler(enabled = invoiceStudentId != null || selectedStudentId != null || selectedClassId != null) {
        when {
            invoiceStudentId != null -> viewModel.openInvoice(null)
            selectedStudentId != null -> viewModel.selectStudent(null)
            selectedClassId != null -> viewModel.selectClass(null)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            // Screen 4: Invoice / Fee Slip
            invoiceStudentId != null && invoiceStudentSummary != null -> {
                InvoiceScreen(
                    summary = invoiceStudentSummary!!,
                    branding = schoolBranding,
                    onBack = { viewModel.openInvoice(null) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Screen 3: Student Details & Fee Ledger
            selectedStudentId != null && selectedStudentSummary != null -> {
                StudentDetailScreen(
                    summary = selectedStudentSummary!!,
                    currentRole = currentRole,
                    onBack = { viewModel.selectStudent(null) },
                    onOpenInvoice = { viewModel.openInvoice(selectedStudentId) },
                    onRecordFeePayment = { month, paid, expected, discount, mode ->
                        viewModel.recordFeePayment(
                            studentId = selectedStudentId!!,
                            month = month,
                            paidAmount = paid,
                            expectedFee = expected,
                            discount = discount,
                            paymentMode = mode
                        )
                    },
                    onAddFund = { title, amount, month ->
                        viewModel.addFund(selectedStudentId!!, title, amount, month)
                    },
                    onUpdateFund = { fund ->
                        viewModel.updateFund(fund)
                    },
                    onDeleteFund = { fundId ->
                        viewModel.deleteFund(fundId)
                    },
                    onUpdateStudent = { student ->
                        viewModel.updateStudent(student)
                    },
                    onDeleteStudent = {
                        viewModel.deleteStudent(selectedStudentId!!)
                    },
                    onSwitchRoleClick = { showRoleSwitchDialog = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Screen 2: Class Students List
            selectedClassId != null && selectedClass != null -> {
                ClassStudentsScreen(
                    schoolClass = selectedClass!!,
                    students = classStudents,
                    currentRole = currentRole,
                    searchQuery = searchQuery,
                    filter = studentFilter,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterChange = { viewModel.setStudentFilter(it) },
                    onBack = { viewModel.selectClass(null) },
                    onStudentClick = { studentId -> viewModel.selectStudent(studentId) },
                    onOpenInvoice = { studentId -> viewModel.openInvoice(studentId) },
                    onRecordFeePayment = { studentId, month, paid, expected, discount, mode ->
                        viewModel.recordFeePayment(studentId, month, paid, expected, discount, mode)
                    },
                    onAddStudent = { rollNo, name, fatherName, contact, customFee ->
                        viewModel.addStudent(
                            classId = selectedClassId!!,
                            rollNo = rollNo,
                            name = name,
                            fatherName = fatherName,
                            contact = contact,
                            customMonthlyFee = customFee
                        )
                    },
                    onSwitchRoleClick = { showRoleSwitchDialog = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Screen 1: Main Dashboard
            else -> {
                DashboardScreen(
                    stats = overviewStats,
                    classes = classesWithStats,
                    currentRole = currentRole,
                    branding = schoolBranding,
                    onClassClick = { classId -> viewModel.selectClass(classId) },
                    onAddClass = { name, fee -> viewModel.addClass(name, fee) },
                    onUpdateClass = { schoolClass -> viewModel.updateClass(schoolClass) },
                    onDeleteClass = { classId -> viewModel.deleteClass(classId) },
                    onSwitchRoleClick = { showRoleSwitchDialog = true },
                    onUpdateLogo = { uri -> viewModel.updateSchoolLogo(uri) },
                    onUpdateAppIcon = { uri -> viewModel.updateAppIcon(uri) },
                    onResetLogo = { viewModel.resetSchoolLogo() },
                    onResetAppIcon = { viewModel.resetAppIcon() },
                    onUpdateSchoolInfo = { name, tagline -> viewModel.updateSchoolInfo(name, tagline) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (showRoleSwitchDialog) {
        RoleSwitchDialog(
            currentRole = currentRole,
            onDismiss = { showRoleSwitchDialog = false },
            onConfirmSwitch = { targetRole, pin ->
                viewModel.switchRole(targetRole, pin)
            }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

