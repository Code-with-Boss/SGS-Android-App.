package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.MonthNames
import com.example.data.model.StudentFeeSummary
import com.example.data.model.StudentFund
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoicePrinter {

    fun generateInvoiceHtml(
        summary: StudentFeeSummary,
        billingMonth: Int,
        selectedFunds: List<StudentFund>,
        includeArrears: Boolean = true,
        dueDate: String = "10th of this month",
        logoBase64: String? = null,
        schoolName: String = "Scholar Grammar High School",
        schoolSubtitle: String = "Govt. Reg. Quality Education • Character Building"
    ): String {
        val student = summary.student
        val schoolClass = summary.schoolClass
        val monthName = MonthNames.getOrElse(billingMonth - 1) { "Current Month" }
        val issueDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val monthlyFee = summary.monthlyRecords[billingMonth]?.expectedFee
            ?: (student.customMonthlyFee ?: (schoolClass?.monthlyFee ?: 2000.0))

        val arrears = if (includeArrears) summary.totalArrearsUpToMonth else 0.0
        val fundsTotal = selectedFunds.sumOf { it.amount }
        val totalPayable = monthlyFee + arrears + fundsTotal

        val fundsRowsHtml = if (selectedFunds.isNotEmpty()) {
            selectedFunds.joinToString("") { fund ->
                """
                <tr>
                    <td style="padding: 8px; border-bottom: 1px solid #e2e8f0;">${fund.title}</td>
                    <td style="padding: 8px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: 600;">Rs. ${fund.amount.toInt()}</td>
                </tr>
                """.trimIndent()
            }
        } else ""

        val arrearsRowHtml = if (arrears > 0) {
            """
            <tr style="background-color: #fef2f2;">
                <td style="padding: 8px; border-bottom: 1px solid #e2e8f0; color: #b91c1c; font-weight: bold;">Previous Outstanding Arrears (بقایا جات)</td>
                <td style="padding: 8px; border-bottom: 1px solid #e2e8f0; text-align: right; color: #b91c1c; font-weight: bold;">Rs. ${arrears.toInt()}</td>
            </tr>
            """.trimIndent()
        } else ""

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Fee Challan - ${student.name}</title>
            <style>
                body {
                    font-family: 'Helvetica Neue', Arial, sans-serif;
                    margin: 0;
                    padding: 24px;
                    color: #1e293b;
                    background-color: #ffffff;
                }
                .invoice-card {
                    max-width: 650px;
                    margin: 0 auto;
                    border: 2px solid #0f2042;
                    border-radius: 12px;
                    padding: 24px;
                }
                .school-header {
                    text-align: center;
                    border-bottom: 2px solid #0f2042;
                    padding-bottom: 16px;
                    margin-bottom: 16px;
                }
                .school-title {
                    font-size: 24px;
                    font-weight: 900;
                    color: #0f2042;
                    letter-spacing: 1px;
                    margin: 0;
                    text-transform: uppercase;
                }
                .school-subtitle {
                    font-size: 13px;
                    color: #64748b;
                    margin: 4px 0 0 0;
                    font-weight: 500;
                }
                .challan-badge {
                    display: inline-block;
                    background: #0f2042;
                    color: #ffffff;
                    padding: 4px 14px;
                    border-radius: 20px;
                    font-size: 13px;
                    font-weight: bold;
                    margin-top: 10px;
                    letter-spacing: 0.5px;
                }
                .meta-table {
                    width: 100%;
                    margin-bottom: 18px;
                    font-size: 14px;
                }
                .meta-table td {
                    padding: 4px 0;
                }
                .meta-label {
                    color: #64748b;
                    width: 140px;
                }
                .meta-value {
                    font-weight: 700;
                    color: #0f172a;
                }
                .fee-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 12px;
                    margin-bottom: 16px;
                    font-size: 14px;
                }
                .fee-table th {
                    background: #f1f5f9;
                    color: #334155;
                    font-weight: 700;
                    text-align: left;
                    padding: 10px 8px;
                    border-bottom: 2px solid #cbd5e1;
                }
                .total-box {
                    background: #f8fafc;
                    border: 1px dashed #0f2042;
                    border-radius: 8px;
                    padding: 12px 16px;
                    display: flex;
                    justify-content: space-between;
                    font-size: 18px;
                    font-weight: 900;
                    color: #0f2042;
                    margin-top: 12px;
                }
                .signatures {
                    margin-top: 48px;
                    display: flex;
                    justify-content: space-between;
                    text-align: center;
                }
                .sig-line {
                    width: 200px;
                    border-top: 1.5px solid #475569;
                    padding-top: 6px;
                    font-size: 13px;
                    font-weight: 600;
                    color: #334155;
                }
                .note {
                    margin-top: 24px;
                    font-size: 11px;
                    color: #64748b;
                    text-align: center;
                    font-style: italic;
                }
            </style>
        </head>
        <body>
            <div class="invoice-card">
                <div class="school-header">
                    ${if (logoBase64 != null) "<img src=\"$logoBase64\" style=\"width: 60px; height: 60px; border-radius: 50%; object-fit: cover; border: 2px solid #0f2042; margin-bottom: 8px;\" alt=\"Logo\" /><br/>" else ""}
                    <h1 class="school-title">$schoolName</h1>
                    <p class="school-subtitle">$schoolSubtitle</p>
                    <div class="challan-badge">OFFICIAL FEE CHALLAN / INVOICE</div>
                </div>

                <table class="meta-table">
                    <tr>
                        <td class="meta-label">Invoice No:</td>
                        <td class="meta-value">SG-${student.rollNo}-${billingMonth}</td>
                        <td class="meta-label">Issue Date:</td>
                        <td class="meta-value">$issueDate</td>
                    </tr>
                    <tr>
                        <td class="meta-label">Student Name:</td>
                        <td class="meta-value">${student.name}</td>
                        <td class="meta-label">Due Date:</td>
                        <td class="meta-value" style="color: #dc2626;">$dueDate</td>
                    </tr>
                    <tr>
                        <td class="meta-label">Father's Name:</td>
                        <td class="meta-value">${student.fatherName}</td>
                        <td class="meta-label">Class & Section:</td>
                        <td class="meta-value">${schoolClass?.name ?: "N/A"}</td>
                    </tr>
                    <tr>
                        <td class="meta-label">Student ID / Roll:</td>
                        <td class="meta-value">${student.rollNo}</td>
                        <td class="meta-label">Contact:</td>
                        <td class="meta-value">${student.contact}</td>
                    </tr>
                </table>

                <table class="fee-table">
                    <thead>
                        <tr>
                            <th>Particulars / Description</th>
                            <th style="text-align: right;">Amount (PKR)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="padding: 8px; border-bottom: 1px solid #e2e8f0;">
                                Tuition Fee ($monthName 2026)
                            </td>
                            <td style="padding: 8px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: 600;">
                                Rs. ${monthlyFee.toInt()}
                            </td>
                        </tr>
                        $fundsRowsHtml
                        $arrearsRowHtml
                    </tbody>
                </table>

                <div class="total-box">
                    <span>NET TOTAL PAYABLE (واجب الادا رقم):</span>
                    <span>Rs. ${totalPayable.toInt()}</span>
                </div>

                <div class="signatures">
                    <div class="sig-line">
                        Parent / Guardian Signature<br>
                        <span style="font-size: 10px; font-weight: normal; color: #94a3b8;">پیرنٹ سگنیچر</span>
                    </div>
                    <div class="sig-line">
                        Teacher / Accountant Signature<br>
                        <span style="font-size: 10px; font-weight: normal; color: #94a3b8;">ٹیچر سگنیچر</span>
                    </div>
                </div>

                <p class="note">
                    * Please deposit the fee before the due date to avoid late payment surcharge. This is a computer-generated fee invoice from Scholar Grammar High School.
                </p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun printInvoice(context: Context, htmlContent: String, jobName: String = "Fee_Invoice") {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager?.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("id", "label", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }

    fun shareInvoiceText(
        context: Context,
        summary: StudentFeeSummary,
        billingMonth: Int,
        selectedFunds: List<StudentFund>,
        includeArrears: Boolean = true
    ) {
        val student = summary.student
        val monthName = MonthNames.getOrElse(billingMonth - 1) { "Current Month" }
        val monthlyFee = summary.monthlyRecords[billingMonth]?.expectedFee
            ?: (student.customMonthlyFee ?: (summary.schoolClass?.monthlyFee ?: 2000.0))
        val arrears = if (includeArrears) summary.totalArrearsUpToMonth else 0.0
        val fundsTotal = selectedFunds.sumOf { it.amount }
        val totalPayable = monthlyFee + arrears + fundsTotal

        val fundsText = if (selectedFunds.isNotEmpty()) {
            selectedFunds.joinToString("\n") { "• ${it.title}: Rs. ${it.amount.toInt()}" } + "\n"
        } else ""

        val arrearsText = if (arrears > 0) "• Previous Arrears (بقایا جات): Rs. ${arrears.toInt()}\n" else ""

        val text = """
        *SCHOLAR GRAMMAR HIGH SCHOOL*
        *Fee Challan - $monthName 2026*
        ----------------------------------
        *Student Name:* ${student.name}
        *Father's Name:* ${student.fatherName}
        *Class:* ${summary.schoolClass?.name ?: "N/A"}
        *Roll No / ID:* ${student.rollNo}
        *Contact:* ${student.contact}
        ----------------------------------
        *Fee Breakdown:*
        • Monthly Tuition: Rs. ${monthlyFee.toInt()}
        $fundsText$arrearsText----------------------------------
        *TOTAL PAYABLE:* *Rs. ${totalPayable.toInt()}*
        ----------------------------------
        Kindly submit the due fee before the 10th of the month.
        Thank you.
        Scholar Grammar High School
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Fee Challan")
        context.startActivity(shareIntent)
    }
}
