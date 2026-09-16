package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.data.model.MonthNames
import com.example.data.model.Student
import com.example.data.model.StudentFeeSummary
import com.example.data.model.StudentFund
import com.example.data.model.UserRole
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberPending
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessBg
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoseDueBg
import com.example.ui.theme.SchoolBlue
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    summary: StudentFeeSummary,
    currentRole: UserRole,
    onBack: () -> Unit,
    onOpenInvoice: () -> Unit,
    onRecordFeePayment: (month: Int, paid: Double, expected: Double, discount: Double, mode: String) -> Unit,
    onAddFund: (title: String, amount: Double, month: Int) -> Unit,
    onUpdateFund: (StudentFund) -> Unit,
    onDeleteFund: (Long) -> Unit,
    onUpdateStudent: (Student) -> Unit,
    onDeleteStudent: () -> Unit,
    onSwitchRoleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val student = summary.student
    val schoolClass = summary.schoolClass
    val standardFee = student.customMonthlyFee ?: (schoolClass?.monthlyFee ?: 2000.0)

    var showEditStudentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddFundDialog by remember { mutableStateOf(false) }
    var feePaymentMonth by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Student Ledger & Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_to_class")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SchoolNavy)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate100),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Student Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = SchoolNavy,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = student.rollNo,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = student.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Father: ${student.fatherName}",
                                        fontSize = 13.sp,
                                        color = Slate600
                                    )
                                }
                            }

                            if (currentRole == UserRole.ADMIN) {
                                Row {
                                    IconButton(
                                        onClick = { showEditStudentDialog = true },
                                        modifier = Modifier.testTag("edit_student_profile_button")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SchoolBlue)
                                    }
                                    IconButton(
                                        onClick = { showDeleteConfirm = true },
                                        modifier = Modifier.testTag("delete_student_profile_button")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseDue)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate100)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Class & Section", fontSize = 11.sp, color = Slate600)
                                Text(schoolClass?.name ?: "N/A", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SchoolNavy)
                            }
                            Column {
                                Text("Monthly Fee", fontSize = 11.sp, color = Slate600)
                                Text("Rs. ${standardFee.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SchoolNavy)
                            }
                            Column {
                                Text("Contact", fontSize = 11.sp, color = Slate600)
                                Text(student.contact, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Actions: Direct Call & WhatsApp & Invoice
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${student.contact}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp), tint = SchoolBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", fontSize = 12.sp, color = SchoolBlue)
                            }

                            Button(
                                onClick = onOpenInvoice,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("detail_generate_invoice_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Generate Invoice / چالان", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Annual Summary Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Paid This Year (ادا شدہ):", fontSize = 12.sp, color = Slate600)
                            Text(
                                "Rs. ${summary.totalPaid.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldSuccess
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Outstanding (بقایا جات):", fontSize = 12.sp, color = Slate600)
                            Text(
                                "Rs. ${summary.totalOutstandingYear.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (summary.totalOutstandingYear > 0) RoseDue else EmeraldSuccess
                            )
                        }
                    }
                }
            }

            // 12 Months Fee Ledger (Jan to Dec)
            item {
                Text(
                    text = "12 Months Tuition Fee Ledger (2026)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SchoolNavy
                )
            }

            for (m in 1..12) {
                item(key = "month_$m") {
                    val record = summary.monthlyRecords[m]
                    val monthName = MonthNames[m - 1]
                    val isPaid = (record?.paidAmount ?: 0.0) >= (record?.expectedFee ?: standardFee)
                    val isPartial = (record?.paidAmount ?: 0.0) > 0 && !isPaid

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = monthName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Slate900
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(status = record?.status ?: "UNPAID")
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Expected: Rs. ${standardFee.toInt()} • Paid: Rs. ${record?.paidAmount?.toInt() ?: 0}" +
                                            if (record?.paymentDate != null) " on ${record.paymentDate}" else "",
                                    fontSize = 11.5.sp,
                                    color = Slate600
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isPaid) {
                                    TextButton(
                                        onClick = {
                                            // Quick mark full paid
                                            onRecordFeePayment(m, standardFee, standardFee, 0.0, "Cash")
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = EmeraldSuccess)
                                    ) {
                                        Text("Full Pay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { feePaymentMonth = m },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("edit_fee_month_$m")
                                ) {
                                    Text(if (isPaid) "Edit" else "Pay", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // Extra Funds & Charges Section (فیس اور فنڈ)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Extra Funds & Charges (فیس اور فنڈ)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SchoolNavy
                        )
                        Text(
                            text = "Exam, paper, sports & miscellaneous funds",
                            fontSize = 11.5.sp,
                            color = Slate600
                        )
                    }

                    if (currentRole == UserRole.ADMIN) {
                        OutlinedButton(
                            onClick = { showAddFundDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("add_fund_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Fund", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (summary.funds.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate200.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("No extra fund charges assigned.", fontSize = 12.sp, color = Slate600)
                        }
                    }
                }
            } else {
                summary.funds.forEach { fund ->
                    item(key = "fund_${fund.id}") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(fund.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                    Text(
                                        text = "Amount: Rs. ${fund.amount.toInt()} • " +
                                                (if (fund.month > 0) MonthNames[fund.month - 1] else "Annual"),
                                        fontSize = 11.5.sp,
                                        color = Slate600
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (fund.isPaid) EmeraldSuccessBg else RoseDueBg,
                                        modifier = Modifier.clickable {
                                            // Toggle fund payment status
                                            onUpdateFund(fund.copy(isPaid = !fund.isPaid))
                                        }
                                    ) {
                                        Text(
                                            text = if (fund.isPaid) "Paid ✓" else "Unpaid ✗",
                                            color = if (fund.isPaid) EmeraldSuccess else RoseDue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    if (currentRole == UserRole.ADMIN) {
                                        IconButton(onClick = { onDeleteFund(fund.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Fund", tint = RoseDue, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Spacing & Print Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOpenInvoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("bottom_generate_invoice_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Print Official Fee Invoice / چالان پرنٹ کریں", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal: Record/Edit Fee for Specific Month
    feePaymentMonth?.let { m ->
        val record = summary.monthlyRecords[m]
        val existingPaid = record?.paidAmount ?: standardFee

        var amountPaidStr by remember { mutableStateOf(existingPaid.toInt().toString()) }
        var discountStr by remember { mutableStateOf((record?.discount ?: 0.0).toInt().toString()) }
        var paymentMode by remember { mutableStateOf(record?.paymentMode ?: "Cash") }

        AlertDialog(
            onDismissRequest = { feePaymentMonth = null },
            title = {
                Text("Fee Payment for ${MonthNames[m - 1]} 2026", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Expected Fee: Rs. ${standardFee.toInt()}", fontSize = 13.sp, color = Slate700)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountPaidStr,
                        onValueChange = { amountPaidStr = it },
                        label = { Text("Amount Paid (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_modal_amount_paid")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = discountStr,
                        onValueChange = { discountStr = it },
                        label = { Text("Concession / Discount (PKR)") },
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
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (paymentMode == mode) SchoolNavy else Slate200,
                                modifier = Modifier
                                    .clickable { paymentMode = mode }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 11.sp,
                                    color = if (paymentMode == mode) Color.White else Slate700,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val paid = amountPaidStr.toDoubleOrNull() ?: 0.0
                        val disc = discountStr.toDoubleOrNull() ?: 0.0
                        onRecordFeePayment(m, paid, standardFee, disc, paymentMode)
                        feePaymentMonth = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                    modifier = Modifier.testTag("save_month_fee_button")
                ) {
                    Text("Save Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { feePaymentMonth = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Add Custom Fund Item
    if (showAddFundDialog) {
        var fundTitle by remember { mutableStateOf("") }
        var fundAmountStr by remember { mutableStateOf("500") }
        var fundMonth by remember { mutableStateOf(3) }

        AlertDialog(
            onDismissRequest = { showAddFundDialog = false },
            title = { Text("Add Extra Fund / Charge", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = fundTitle,
                        onValueChange = { fundTitle = it },
                        label = { Text("Fund Title") },
                        placeholder = { Text("e.g. Exam Fund, Paper Charges") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fund_title")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fundAmountStr,
                        onValueChange = { fundAmountStr = it },
                        label = { Text("Amount (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fund_amount")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = fundAmountStr.toDoubleOrNull() ?: 0.0
                        if (fundTitle.isNotBlank() && amt > 0) {
                            onAddFund(fundTitle.trim(), amt, fundMonth)
                            showAddFundDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                    modifier = Modifier.testTag("save_fund_confirm")
                ) {
                    Text("Add Fund")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFundDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Edit Student Profile (Admin)
    if (showEditStudentDialog) {
        var rollNo by remember { mutableStateOf(student.rollNo) }
        var name by remember { mutableStateOf(student.name) }
        var fatherName by remember { mutableStateOf(student.fatherName) }
        var contact by remember { mutableStateOf(student.contact) }
        var feeStr by remember { mutableStateOf((student.customMonthlyFee ?: standardFee).toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showEditStudentDialog = false },
            title = { Text("Edit Student Details", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = rollNo,
                        onValueChange = { rollNo = it },
                        label = { Text("Roll No / Unique ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Student Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("Father's Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        label = { Text("Contact Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = feeStr,
                        onValueChange = { feeStr = it },
                        label = { Text("Monthly Fee (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newFee = feeStr.toDoubleOrNull()
                        onUpdateStudent(
                            student.copy(
                                rollNo = rollNo.trim(),
                                name = name.trim(),
                                fatherName = fatherName.trim(),
                                contact = contact.trim(),
                                customMonthlyFee = newFee
                            )
                        )
                        showEditStudentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditStudentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Delete Confirmation (Admin)
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Student Record") },
            text = { Text("Are you sure you want to remove ${student.name} (${student.rollNo})? All fee history will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteStudent()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDue)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
