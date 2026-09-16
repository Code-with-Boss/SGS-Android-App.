package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ClassWithStats
import com.example.data.model.SchoolBranding
import com.example.data.model.SchoolClass
import com.example.data.model.SchoolOverviewStats
import com.example.data.model.UserRole
import com.example.ui.components.AdminBrandingDialog
import com.example.ui.components.RoleBadge
import com.example.ui.components.SchoolLogoView
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessBg
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoseDueBg
import com.example.ui.theme.SchoolBlue
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolGoldLight
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolNavyDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun DashboardScreen(
    stats: SchoolOverviewStats,
    classes: List<ClassWithStats>,
    currentRole: UserRole,
    branding: SchoolBranding = SchoolBranding(),
    onClassClick: (Long) -> Unit,
    onAddClass: (String, Double) -> Unit,
    onUpdateClass: (SchoolClass) -> Unit,
    onDeleteClass: (Long) -> Unit,
    onSwitchRoleClick: () -> Unit,
    onUpdateLogo: (Uri) -> Unit = {},
    onUpdateAppIcon: (Uri) -> Unit = {},
    onResetLogo: () -> Unit = {},
    onResetAppIcon: () -> Unit = {},
    onUpdateSchoolInfo: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showAddClassDialog by remember { mutableStateOf(false) }
    var showBrandingDialog by remember { mutableStateOf(false) }
    var classToEdit by remember { mutableStateOf<SchoolClass?>(null) }
    var classToDelete by remember { mutableStateOf<SchoolClass?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header Institutional Banner
            item {
                SchoolHeaderBanner(
                    branding = branding,
                    currentRole = currentRole,
                    onSwitchRoleClick = onSwitchRoleClick,
                    onOpenBrandingDialog = { showBrandingDialog = true }
                )
            }

            // Admin Quick Branding & Logo Card
            if (currentRole == UserRole.ADMIN) {
                item {
                    AdminBrandingQuickCard(
                        branding = branding,
                        onClick = { showBrandingDialog = true }
                    )
                }
            }

            // Overview Summary Metrics
            item {
                OverviewStatsCard(stats = stats)
            }

            // Classes Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Classes & Fee Structure",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SchoolNavy
                        )
                        Text(
                            text = "کلاسیں اور ماہانہ فیس سیکشن (${classes.size} Classes)",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }

                    if (currentRole == UserRole.ADMIN) {
                        OutlinedButton(
                            onClick = { showAddClassDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SchoolNavy
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_class_header_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Class", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Classes List
            if (classes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Slate600, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No classes added yet", fontWeight = FontWeight.Bold, color = Slate700)
                        }
                    }
                }
            } else {
                items(classes, key = { it.schoolClass.id }) { item ->
                    ClassCardItem(
                        item = item,
                        isAdmin = currentRole == UserRole.ADMIN,
                        onClick = { onClassClick(item.schoolClass.id) },
                        onEdit = { classToEdit = item.schoolClass },
                        onDelete = { classToDelete = item.schoolClass }
                    )
                }
            }
        }

        // Admin Floating Action Button
        if (currentRole == UserRole.ADMIN) {
            FloatingActionButton(
                onClick = { showAddClassDialog = true },
                containerColor = SchoolNavy,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("fab_add_class")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Class")
            }
        }
    }

    // Dialog: Add Class
    if (showAddClassDialog) {
        AddEditClassDialog(
            classToEdit = null,
            onDismiss = { showAddClassDialog = false },
            onConfirm = { name, fee ->
                onAddClass(name, fee)
                showAddClassDialog = false
            }
        )
    }

    // Dialog: Edit Class
    classToEdit?.let { cls ->
        AddEditClassDialog(
            classToEdit = cls,
            onDismiss = { classToEdit = null },
            onConfirm = { name, fee ->
                onUpdateClass(cls.copy(name = name, monthlyFee = fee))
                classToEdit = null
            }
        )
    }

    // Dialog: Delete Class Confirmation
    classToDelete?.let { cls ->
        AlertDialog(
            onDismissRequest = { classToDelete = null },
            title = { Text("Delete Class") },
            text = { Text("Are you sure you want to delete '${cls.name}' and all its student records? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClass(cls.id)
                        classToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDue)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { classToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: School Branding & Logo Settings (Admin Only)
    if (showBrandingDialog && currentRole == UserRole.ADMIN) {
        AdminBrandingDialog(
            branding = branding,
            onDismiss = { showBrandingDialog = false },
            onUpdateLogo = onUpdateLogo,
            onUpdateAppIcon = onUpdateAppIcon,
            onResetLogo = onResetLogo,
            onResetAppIcon = onResetAppIcon,
            onUpdateSchoolInfo = onUpdateSchoolInfo
        )
    }
}

@Composable
fun SchoolHeaderBanner(
    branding: SchoolBranding,
    currentRole: UserRole,
    onSwitchRoleClick: () -> Unit,
    onOpenBrandingDialog: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(SchoolNavy, SchoolBlue)
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(enabled = currentRole == UserRole.ADMIN) {
                            onOpenBrandingDialog()
                        }
                    ) {
                        Box(modifier = Modifier.size(56.dp)) {
                            SchoolLogoView(
                                branding = branding,
                                isAppIcon = false,
                                modifier = Modifier.size(56.dp)
                            )
                            if (currentRole == UserRole.ADMIN) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(SchoolGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Change Logo",
                                        tint = SchoolNavy,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = branding.schoolName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = branding.schoolTagline,
                                color = SchoolGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    RoleBadge(
                        role = currentRole,
                        onSwitchRoleClick = onSwitchRoleClick
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fee Management System • Academic Session 2026",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (currentRole == UserRole.ADMIN) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { onOpenBrandingDialog() }
                                    .padding(start = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = SchoolGoldLight,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "لوگو بدلیں",
                                    color = SchoolGoldLight,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "Teacher Mode",
                                color = EmeraldSuccessBg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBrandingQuickCard(
    branding: SchoolBranding,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .border(1.5.dp, SchoolNavy, CircleShape)
                        .padding(2.dp)
                ) {
                    SchoolLogoView(
                        branding = branding,
                        isAppIcon = false,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .border(1.5.dp, SchoolBlue, RoundedCornerShape(10.dp))
                        .padding(2.dp)
                ) {
                    SchoolLogoView(
                        branding = branding,
                        isAppIcon = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "School Logos & App Icon",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = SchoolNavy
                    )
                    Text(
                        text = if (branding.schoolLogoPath != null || branding.appIconPath != null)
                            "لوکل اسٹوریج میں محفوظ • چالان پر فعال"
                        else
                            "گیلری سے سکول کا لوگو یا آئیکون تبدیل کریں",
                        fontSize = 11.sp,
                        color = if (branding.schoolLogoPath != null) EmeraldSuccess else Slate600
                    )
                }
            }

            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SchoolNavy),
                modifier = Modifier.testTag("change_logo_quick_button")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تبدیل کریں", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OverviewStatsCard(stats: SchoolOverviewStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Financial Overview (سالانہ مالیاتی جائزہ)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate700
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Students & Classes
                MetricMiniCard(
                    title = "Students",
                    value = "${stats.totalStudents}",
                    subtitle = "${stats.totalClasses} Classes",
                    icon = Icons.Default.Groups,
                    color = SchoolBlue,
                    modifier = Modifier.weight(1f)
                )

                // Total Collected
                MetricMiniCard(
                    title = "Collected",
                    value = "Rs. ${(stats.totalCollectedYear / 1000).toInt()}k",
                    subtitle = "Received this year",
                    icon = Icons.Default.Payments,
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )

                // Total Outstanding
                MetricMiniCard(
                    title = "Outstanding",
                    value = "Rs. ${(stats.totalOutstandingYear / 1000).toInt()}k",
                    subtitle = "Remaining dues",
                    icon = Icons.Default.TrendingUp,
                    color = RoseDue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Slate900)
            Text(subtitle, fontSize = 9.5.sp, color = Slate600)
        }
    }
}

@Composable
fun ClassCardItem(
    item: ClassWithStats,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("class_card_${item.schoolClass.name.replace(" ", "_")}"),
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
                        shape = RoundedCornerShape(8.dp),
                        color = SchoolNavy.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Class, contentDescription = null, tint = SchoolNavy, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.schoolClass.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SchoolNavyDark
                        )
                        Text(
                            text = "${item.studentCount} Students enrolled",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }
                }

                // Dedicated Fee Section for each class (MANDATED: "ان کے اگے ایک فیس کا سیکشن ہو، ہر کلاس کی یہ یہ فیس ہے")
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SchoolGold.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SchoolGold.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "MONTHLY FEE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = SchoolGold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Rs. ${item.schoolClass.monthlyFee.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Slate900
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress & Financial stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Collected: ", fontSize = 11.sp, color = Slate600)
                    Text("Rs. ${item.totalCollected.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Outstanding: ", fontSize = 11.sp, color = Slate600)
                    Text("Rs. ${item.totalOutstanding.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseDue)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAdmin) {
                    Row {
                        TextButton(
                            onClick = onEdit,
                            modifier = Modifier.testTag("edit_class_${item.schoolClass.name.replace(" ", "_")}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Class", modifier = Modifier.size(14.dp), tint = SchoolBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Fee / Name", fontSize = 12.sp, color = SchoolBlue, fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Class", modifier = Modifier.size(14.dp), tint = RoseDue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", fontSize = 12.sp, color = RoseDue)
                        }
                    }
                } else {
                    Text("Teacher Access: View & Enter Fees", fontSize = 11.sp, color = Slate600)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Text(
                        text = "View Students",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SchoolNavy
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "View Students",
                        tint = SchoolNavy,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditClassDialog(
    classToEdit: SchoolClass?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, fee: Double) -> Unit
) {
    var className by remember { mutableStateOf(classToEdit?.name ?: "") }
    var monthlyFeeStr by remember { mutableStateOf(classToEdit?.monthlyFee?.toInt()?.toString() ?: "2500") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (classToEdit == null) "Add New Class" else "Edit Class & Fee",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Customize the class name and default monthly tuition fee:",
                    fontSize = 12.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = className,
                    onValueChange = {
                        className = it
                        error = null
                    },
                    label = { Text("Class Name") },
                    placeholder = { Text("e.g. Class 1 A, Playgroup B") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_class_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = monthlyFeeStr,
                    onValueChange = {
                        monthlyFeeStr = it
                        error = null
                    },
                    label = { Text("Monthly Tuition Fee (PKR)") },
                    placeholder = { Text("e.g. 2500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_class_fee")
                )

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = RoseDue, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = monthlyFeeStr.toDoubleOrNull()
                    if (className.isBlank()) {
                        error = "Class name cannot be empty"
                    } else if (fee == null || fee <= 0) {
                        error = "Please enter a valid monthly fee"
                    } else {
                        onConfirm(className.trim(), fee)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                modifier = Modifier.testTag("save_class_button")
            ) {
                Text(if (classToEdit == null) "Add Class" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
