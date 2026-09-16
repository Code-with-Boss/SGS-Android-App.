package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MonthNames
import com.example.data.model.MonthShortNames
import com.example.data.model.SchoolBranding
import com.example.data.model.StudentFeeSummary
import com.example.data.model.StudentFund
import com.example.ui.components.SchoolLogoView
import com.example.ui.theme.EmeraldSuccess
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
import com.example.util.BrandingManager
import com.example.util.InvoicePrinter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    summary: StudentFeeSummary,
    branding: SchoolBranding = SchoolBranding(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val student = summary.student
    val schoolClass = summary.schoolClass

    var billingMonth by remember { mutableStateOf(3) } // March default or current month
    var includeArrears by remember { mutableStateOf(true) }
    var dueDate by remember { mutableStateOf("10th of this month") }

    // Dynamic funds selected to include in invoice
    val includedFunds = remember(summary.funds) {
        mutableStateListOf<StudentFund>().apply { addAll(summary.funds) }
    }

    var customFundTitle by remember { mutableStateOf("") }
    var customFundAmount by remember { mutableStateOf("") }

    val monthlyFee = summary.monthlyRecords[billingMonth]?.expectedFee
        ?: (student.customMonthlyFee ?: (schoolClass?.monthlyFee ?: 2000.0))

    val arrearsAmount = if (includeArrears) summary.totalArrearsUpToMonth else 0.0
    val fundsTotal = includedFunds.sumOf { it.amount }
    val totalPayable = monthlyFee + arrearsAmount + fundsTotal

    val todayDate = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Official Fee Slip / انوائس",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_from_invoice")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val html = InvoicePrinter.generateInvoiceHtml(
                                summary = summary,
                                billingMonth = billingMonth,
                                selectedFunds = includedFunds.toList(),
                                includeArrears = includeArrears,
                                dueDate = dueDate,
                                logoBase64 = BrandingManager.getLogoBase64(context),
                                schoolName = branding.schoolName,
                                schoolSubtitle = branding.schoolTagline
                            )
                            InvoicePrinter.printInvoice(context, html, "Invoice_${student.rollNo}")
                        },
                        modifier = Modifier.testTag("top_print_invoice_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            InvoicePrinter.shareInvoiceText(
                                context = context,
                                summary = summary,
                                billingMonth = billingMonth,
                                selectedFunds = includedFunds.toList(),
                                includeArrears = includeArrears
                            )
                        },
                        modifier = Modifier.testTag("top_share_invoice_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
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
            // Configuration controls card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Invoice Settings (چالان ترتیبات)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SchoolNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Select Billing Month:", fontSize = 12.sp, color = Slate700)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..6).forEach { m ->
                                FilterChip(
                                    selected = billingMonth == m,
                                    onClick = { billingMonth = m },
                                    label = { Text(MonthShortNames[m - 1], fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (7..12).forEach { m ->
                                FilterChip(
                                    selected = billingMonth == m,
                                    onClick = { billingMonth = m },
                                    label = { Text(MonthShortNames[m - 1], fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeArrears,
                                onCheckedChange = { includeArrears = it },
                                colors = CheckboxDefaults.colors(checkedColor = SchoolNavy)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Include Previous Arrears / بقایا جات (Rs. ${summary.totalArrearsUpToMonth.toInt()})",
                                fontSize = 13.sp,
                                color = if (summary.totalArrearsUpToMonth > 0) RoseDue else Slate700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Official Fee Slip / Challan Preview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, SchoolNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // School Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SchoolLogoView(
                                branding = branding,
                                isAppIcon = false,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = branding.schoolName.uppercase(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SchoolNavy,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = branding.schoolTagline,
                                    fontSize = 10.5.sp,
                                    color = Slate600
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SchoolNavy)
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "STUDENT FEE CHALLAN / انوائس",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Slate200)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Metadata Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Challan No: SG-${student.rollNo}-$billingMonth", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate800)
                                Text("Issue Date: $todayDate", fontSize = 11.sp, color = Slate600)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Billing: ${MonthNames[billingMonth - 1]} 2026", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = SchoolNavy)
                                Text("Due Date: $dueDate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RoseDue)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Student Info Box
                        Surface(
                            color = Slate100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Student Name:", fontSize = 11.5.sp, color = Slate600)
                                    Text(student.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Father Name:", fontSize = 11.5.sp, color = Slate600)
                                    Text(student.fatherName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Class & Section:", fontSize = 11.5.sp, color = Slate600)
                                    Text(schoolClass?.name ?: "N/A", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SchoolNavy)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Student ID / Roll No:", fontSize = 11.5.sp, color = Slate600)
                                    Text(student.rollNo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SchoolNavy)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Contact Phone:", fontSize = 11.5.sp, color = Slate600)
                                    Text(student.contact, fontSize = 12.sp, color = Slate800)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Particulars / Fee Table
                        Text(
                            text = "FEE PARTICULARS / تفصیلات فیس",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                // Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Slate100)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Description", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                                    Text("Amount (PKR)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                                }
                                HorizontalDivider(color = Slate200)

                                // Monthly Tuition
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Monthly Tuition (${MonthNames[billingMonth - 1]})", fontSize = 12.sp, color = Slate900)
                                    Text("Rs. ${monthlyFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                }

                                // Extra funds rows
                                includedFunds.forEach { fund ->
                                    HorizontalDivider(color = Slate100)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(fund.title, fontSize = 12.sp, color = Slate800)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Rs. ${fund.amount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "✕",
                                                color = RoseDue,
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .clickable { includedFunds.remove(fund) }
                                                    .padding(2.dp)
                                            )
                                        }
                                    }
                                }

                                // Arrears row
                                if (arrearsAmount > 0) {
                                    HorizontalDivider(color = Slate200)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(RoseDueBg.copy(alpha = 0.5f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Previous Outstanding Arrears (بقایا جات)", fontSize = 12.sp, color = RoseDue, fontWeight = FontWeight.Bold)
                                        Text("Rs. ${arrearsAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = RoseDue)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // NET TOTAL PAYABLE BOX (MANDATORY: "اس کا نا جو بقایا جو پے ایبل فیس ہے نا جو اس نے پے کرنی ہے اس کا ٹوٹل فی... ٹھیک ہے اس کو ٹوٹل کرنا ہے کہ یہ یہ فیس اپ نے دینی ہے")
                        Surface(
                            color = SchoolNavy,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "NET TOTAL PAYABLE:",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "کل واجب الادا رقم",
                                        color = SchoolGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = "Rs. ${totalPayable.toInt()}",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(36.dp))

                        // Signatures (MANDATORY: "اس کے نیچے پیرنٹ سگنیچر اور ٹیچر سگنیچر کا سیکشن ہو")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Parent Signature
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(130.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Slate700)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Parent's Signature",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                                Text(
                                    text = "(پیرنٹ سگنیچر)",
                                    fontSize = 10.sp,
                                    color = Slate600
                                )
                            }

                            // Teacher Signature
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(130.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Slate700)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Teacher's Signature",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                                Text(
                                    text = "(ٹیچر سگنیچر)",
                                    fontSize = 10.sp,
                                    color = Slate600
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "* Please present this challan at the accounts office during fee submission. Late payment after due date may incur surcharge.",
                            fontSize = 9.5.sp,
                            color = Slate600,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Quick Add Fund into Fee Slip
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "+ Add Extra Charge/Fund into Fee Slip (اخراجات / فنڈ شامل کریں)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SchoolNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customFundTitle,
                                onValueChange = { customFundTitle = it },
                                placeholder = { Text("Fund Name (e.g. Exam/Paper)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f)
                            )
                            OutlinedTextField(
                                value = customFundAmount,
                                onValueChange = { customFundAmount = it },
                                placeholder = { Text("Amount", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val amt = customFundAmount.toDoubleOrNull() ?: 0.0
                                if (customFundTitle.isNotBlank() && amt > 0) {
                                    includedFunds.add(
                                        StudentFund(
                                            id = System.currentTimeMillis(),
                                            studentId = student.id,
                                            title = customFundTitle.trim(),
                                            amount = amt,
                                            month = billingMonth,
                                            year = 2026,
                                            isPaid = false
                                        )
                                    )
                                    customFundTitle = ""
                                    customFundAmount = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SchoolBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add into this Slip", fontSize = 12.5.sp)
                        }
                    }
                }
            }

            // Primary Print & Share Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val html = InvoicePrinter.generateInvoiceHtml(
                                summary = summary,
                                billingMonth = billingMonth,
                                selectedFunds = includedFunds.toList(),
                                includeArrears = includeArrears,
                                dueDate = dueDate,
                                logoBase64 = BrandingManager.getLogoBase64(context),
                                schoolName = branding.schoolName,
                                schoolSubtitle = branding.schoolTagline
                            )
                            InvoicePrinter.printInvoice(context, html, "Invoice_${student.rollNo}")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("print_invoice_primary_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Print / PDF چالان", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            InvoicePrinter.shareInvoiceText(
                                context = context,
                                summary = summary,
                                billingMonth = billingMonth,
                                selectedFunds = includedFunds.toList(),
                                includeArrears = includeArrears
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("share_invoice_whatsapp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share WhatsApp", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
