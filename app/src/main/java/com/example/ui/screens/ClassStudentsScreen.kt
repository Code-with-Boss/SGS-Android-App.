package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthShortNames
import com.example.data.model.SchoolClass
import com.example.data.model.StudentFeeSummary
import com.example.data.model.UserRole
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberPending
import com.example.ui.theme.AmberPendingBg
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessBg
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoseDueBg
import com.example.ui.theme.SchoolBlue
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolNavyDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassStudentsScreen(
    schoolClass: SchoolClass,
    students: List<StudentFeeSummary>,
    currentRole: UserRole,
    searchQuery: String,
    filter: String,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onBack: () -> Unit,
    onStudentClick: (Long) -> Unit,
    onOpenInvoice: (Long) -> Unit,
    onRecordFeePayment: (studentId: Long, month: Int, paid: Double, expected: Double, discount: Double, mode: String) -> Unit,
    onAddStudent: (rollNo: String, name: String, fatherName: String, contact: String, customFee: Double?) -> Unit,
    onSwitchRoleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var paymentStudentSummary by remember { mutableStateOf<StudentFeeSummary?>(null) }

    // Filter students
    val filteredStudents = remember(students, searchQuery, filter) {
        students.filter { summary ->
            val matchQuery = searchQuery.isBlank() ||
                    summary.student.name.contains(searchQuery, ignoreCase = true) ||
                    summary.student.rollNo.contains(searchQuery, ignoreCase = true) ||
                    summary.student.fatherName.contains(searchQuery, ignoreCase = true)

            val matchFilter = when (filter) {
                "PAID" -> summary.totalOutstandingYear <= 0.0
                "DEFAULTER" -> summary.totalOutstandingYear > 0.0
                else -> true
            }

            matchQuery && matchFilter
        }
    }

    val totalClassCollected = remember(students) { students.sumOf { it.totalPaid } }
    val totalClassOutstanding = remember(students) { students.sumOf { it.totalOutstandingYear } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = schoolClass.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Standard Fee: Rs. ${schoolClass.monthlyFee.toInt()} / month",
                            fontSize = 12.sp,
                            color = SchoolGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_to_dashboard")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    RoleBadge(
                        role = currentRole,
                        onSwitchRoleClick = onSwitchRoleClick,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SchoolNavy
                )
            )
        },
        floatingActionButton = {
            if (currentRole == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { showAddStudentDialog = true },
                    containerColor = SchoolNavy,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_student")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Student")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate100)
        ) {
            // Class Summary Header Bar
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Students Roster (${students.size} Enrolled)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                            Text(
                                text = "Collected: Rs. ${totalClassCollected.toInt()} • Remaining: Rs. ${totalClassOutstanding.toInt()}",
                                fontSize = 11.5.sp,
                                color = Slate600
                            )
                        }

                        if (currentRole == UserRole.ADMIN) {
                            Button(
                                onClick = { showAddStudentDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("button_add_student_top")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Student", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_students_input"),
                        placeholder = { Text("Search by Roll No, Name or Father Name...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate400)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Slate400)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filter == "ALL",
                            onClick = { onFilterChange("ALL") },
                            label = { Text("All (${students.size})", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = filter == "PAID",
                            onClick = { onFilterChange("PAID") },
                            label = { Text("Fully Paid", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldSuccessBg,
                                selectedLabelColor = EmeraldSuccess
                            )
                        )
                        FilterChip(
                            selected = filter == "DEFAULTER",
                            onClick = { onFilterChange("DEFAULTER") },
                            label = { Text("Defaulters (باقیات)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoseDueBg,
                                selectedLabelColor = RoseDue
                            )
                        )
                    }
                }
            }

            // Student List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Slate400, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No students found matching '$searchQuery'" else "No students enrolled in ${schoolClass.name}",
                            color = Slate600,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStudents, key = { it.student.id }) { summary ->
                        StudentItemCard(
                            summary = summary,
                            schoolClass = schoolClass,
                            onClick = { onStudentClick(summary.student.id) },
                            onInvoiceClick = { onOpenInvoice(summary.student.id) },
                            onRecordFeeClick = { paymentStudentSummary = summary }
                        )
                    }
                }
            }
        }
    }

    // Add Student Dialog (Admin)
    if (showAddStudentDialog) {
        AddStudentDialog(
            defaultClassFee = schoolClass.monthlyFee,
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { rollNo, name, fatherName, contact, customFee ->
                onAddStudent(rollNo, name, fatherName, contact, customFee)
                showAddStudentDialog = false
            }
        )
    }

    // Quick Fee Payment Dialog (Available for both Teacher and Admin!)
    paymentStudentSummary?.let { summary ->
        QuickFeePaymentDialog(
            summary = summary,
            onDismiss = { paymentStudentSummary = null },
            onConfirm = { month, paidAmount, expectedFee, discount, mode ->
                onRecordFeePayment(summary.student.id, month, paidAmount, expectedFee, discount, mode)
                paymentStudentSummary = null
            }
        )
    }
}

@Composable
fun StudentItemCard(
    summary: StudentFeeSummary,
    schoolClass: SchoolClass,
    onClick: () -> Unit,
    onInvoiceClick: () -> Unit,
    onRecordFeeClick: () -> Unit
) {
    val context = LocalContext.current
    val student = summary.student
    val monthlyFee = student.customMonthlyFee ?: schoolClass.monthlyFee

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_card_${student.rollNo}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Roll No Badge + Student Name & Father Name + Contact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    // Item 1: Personal Roll Number / Unique ID (MANDATORY: "سب سے پہلے نمبر پر اس کا پرسنل رول نمبر ہو، یونیک ائی ڈی")
                    Surface(
                        color = SchoolNavy,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("roll_no_${student.rollNo}")
                    ) {
                        Text(
                            text = student.rollNo,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        // Item 2: Student Name
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                        // Item 3: Father Name
                        Text(
                            text = "S/D of ${student.fatherName}",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }
                }

                // Item 4: Contact Number with Call action
                if (student.contact.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate100,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${student.contact}")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = SchoolBlue, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = student.contact, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = SchoolBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Item 5: Monthly Fee of this student/class (MANDATORY: "پھر اس کلاس کی اس کی فیس کتنی ہے یہ ہو")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Monthly Fee: ", fontSize = 12.sp, color = Slate600)
                    Text(
                        text = "Rs. ${monthlyFee.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SchoolNavy
                    )
                    if (student.customMonthlyFee != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(Special Concession)",
                            fontSize = 10.sp,
                            color = SchoolGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (summary.totalOutstandingYear <= 0) {
                    StatusBadge(status = "PAID")
                } else {
                    StatusBadge(status = "UNPAID")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Item 6: 12-Month Fee Records (Jan, Feb, Mar, Apr... Dec)
            // MANDATORY: "اس کے اگے جنوری، فروری، مارچ، اپریل یہ ڈیٹا ہو کہ اس کا کس مہینے میں کون کون سی فیس ائی ہے"
            Text(
                text = "12 Months Fee Status (2026):",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate700
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (m in 1..12) {
                    val record = summary.monthlyRecords[m]
                    val isPaid = (record?.paidAmount ?: 0.0) >= (record?.expectedFee ?: monthlyFee)
                    val isPartial = (record?.paidAmount ?: 0.0) > 0 && !isPaid
                    val mName = MonthShortNames[m - 1]

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isPaid -> EmeraldSuccessBg
                            isPartial -> AmberPendingBg
                            else -> RoseDueBg
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            when {
                                isPaid -> EmeraldSuccess.copy(alpha = 0.5f)
                                isPartial -> AmberPending.copy(alpha = 0.5f)
                                else -> RoseDue.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = mName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isPaid -> EmeraldSuccess
                                    isPartial -> AmberPending
                                    else -> RoseDue
                                }
                            )
                            Text(
                                text = if (isPaid) "Rs.${record?.paidAmount?.toInt()}" else if (isPartial) "P:Rs.${record?.paidAmount?.toInt()}" else "Due",
                                fontSize = 9.sp,
                                color = when {
                                    isPaid -> EmeraldSuccess
                                    isPartial -> AmberPending
                                    else -> RoseDue
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Item 7 & 8: Total Fee Paid & Total Outstanding (MANDATORY: "اینڈ پہ ہو کہ اس نے ٹوٹل فیس کتنی دی ہے اور اس کے بعد ائے اگلی لائن میں ہو کہ فیس اؤٹ سٹینڈنگ کہ اس نے اب تک اس سال کی جو فیس دی ہیں اس میں سے ٹوٹل بقایا کتنا بچتا ہے")
            Surface(
                color = Slate100,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Fee Paid (ٹوٹل ادا شدہ):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate700
                        )
                        Text(
                            text = "Rs. ${summary.totalPaid.toInt()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Outstanding Balance (فیس بقایا جات):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (summary.totalOutstandingYear > 0) RoseDue else Slate600
                        )
                        Text(
                            text = "Rs. ${summary.totalOutstandingYear.toInt()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (summary.totalOutstandingYear > 0) RoseDue else EmeraldSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Print Fee Invoice / Slip & Record Fee Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onInvoiceClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("invoice_button_${student.rollNo}"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp), tint = SchoolNavy)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fee Slip / انوائس", fontSize = 12.sp, color = SchoolNavy, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRecordFeeClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("record_fee_button_${student.rollNo}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record Fee", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddStudentDialog(
    defaultClassFee: Double,
    onDismiss: () -> Unit,
    onConfirm: (rollNo: String, name: String, fatherName: String, contact: String, customFee: Double?) -> Unit
) {
    var rollNo by remember { mutableStateOf("SG-") }
    var name by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("03") }
    var customFeeStr by remember { mutableStateOf(defaultClassFee.toInt().toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Register New Student", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = rollNo,
                    onValueChange = { rollNo = it; errorMessage = null },
                    label = { Text("Personal Roll No / Unique ID") },
                    placeholder = { Text("e.g. SG-120") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_roll")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Student Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_name")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it; errorMessage = null },
                    label = { Text("Father's Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_father")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it; errorMessage = null },
                    label = { Text("Contact Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_contact")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customFeeStr,
                    onValueChange = { customFeeStr = it; errorMessage = null },
                    label = { Text("Monthly Tuition Fee (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_custom_fee")
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(it, color = RoseDue, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = customFeeStr.toDoubleOrNull()
                    if (rollNo.isBlank() || rollNo == "SG-") {
                        errorMessage = "Please enter a unique Roll Number / ID"
                    } else if (name.isBlank()) {
                        errorMessage = "Please enter Student Name"
                    } else if (fatherName.isBlank()) {
                        errorMessage = "Please enter Father Name"
                    } else {
                        onConfirm(rollNo.trim(), name.trim(), fatherName.trim(), contact.trim(), fee)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                modifier = Modifier.testTag("save_student_confirm")
            ) {
                Text("Register Student")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuickFeePaymentDialog(
    summary: StudentFeeSummary,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, paid: Double, expected: Double, discount: Double, mode: String) -> Unit
) {
    val student = summary.student
    val defaultFee = student.customMonthlyFee ?: (summary.schoolClass?.monthlyFee ?: 2000.0)

    var selectedMonth by remember { mutableStateOf(3) } // default March or whichever
    var paidAmountStr by remember { mutableStateOf(defaultFee.toInt().toString()) }
    var discountStr by remember { mutableStateOf("0") }
    var paymentMode by remember { mutableStateOf("Cash") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Record Fee Payment", fontWeight = FontWeight.Bold)
                Text(
                    text = "${student.name} (${student.rollNo})",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }
        },
        text = {
            Column {
                Text("Select Billing Month:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate700)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (m in 1..12) {
                        FilterChip(
                            selected = selectedMonth == m,
                            onClick = { selectedMonth = m },
                            label = { Text(MonthShortNames[m - 1], fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = paidAmountStr,
                    onValueChange = { paidAmountStr = it },
                    label = { Text("Amount Received (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fee_received")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = discountStr,
                    onValueChange = { discountStr = it },
                    label = { Text("Discount / Concession (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Payment Mode:", fontSize = 12.sp, color = Slate700)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Cash", "EasyPaisa", "JazzCash", "Bank").forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paid = paidAmountStr.toDoubleOrNull() ?: 0.0
                    val disc = discountStr.toDoubleOrNull() ?: 0.0
                    onConfirm(selectedMonth, paid, defaultFee, disc, paymentMode)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                modifier = Modifier.testTag("confirm_record_fee")
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
