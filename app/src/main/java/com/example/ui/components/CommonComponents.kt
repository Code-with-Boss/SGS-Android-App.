package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.AmberPending
import com.example.ui.theme.AmberPendingBg
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessBg
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoseDueBg
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun RoleBadge(
    role: UserRole,
    onSwitchRoleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onSwitchRoleClick() }
            .testTag("role_badge_button"),
        color = if (role == UserRole.ADMIN) SchoolGold.copy(alpha = 0.18f) else EmeraldSuccess.copy(alpha = 0.18f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (role == UserRole.ADMIN) SchoolGold else EmeraldSuccess
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.School,
                contentDescription = null,
                tint = if (role == UserRole.ADMIN) SchoolGold else EmeraldSuccess,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (role == UserRole.ADMIN) "Admin Mode" else "Teacher Mode",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (role == UserRole.ADMIN) SchoolGold else EmeraldSuccess
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Switch Role",
                tint = Slate600,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun RoleSwitchDialog(
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onConfirmSwitch: (UserRole, String) -> Boolean
) {
    var targetRole by remember {
        mutableStateOf(if (currentRole == UserRole.ADMIN) UserRole.TEACHER else UserRole.ADMIN)
    }
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = SchoolNavy
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Switch User Role",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Current role: ${if (currentRole == UserRole.ADMIN) "Administrator (Full Access)" else "Teacher (Fee Entry Access)"}",
                    fontSize = 13.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                targetRole = UserRole.ADMIN
                                errorMessage = null
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (targetRole == UserRole.ADMIN) SchoolNavy.copy(alpha = 0.1f) else Slate200.copy(alpha = 0.4f)
                        ),
                        border = if (targetRole == UserRole.ADMIN) androidx.compose.foundation.BorderStroke(2.dp, SchoolNavy) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = SchoolNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Admin", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Full Control", fontSize = 11.sp, color = Slate600)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                targetRole = UserRole.TEACHER
                                errorMessage = null
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (targetRole == UserRole.TEACHER) EmeraldSuccess.copy(alpha = 0.1f) else Slate200.copy(alpha = 0.4f)
                        ),
                        border = if (targetRole == UserRole.TEACHER) androidx.compose.foundation.BorderStroke(2.dp, EmeraldSuccess) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = EmeraldSuccess)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Teacher", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Fee Entry", fontSize = 11.sp, color = Slate600)
                        }
                    }
                }

                if (targetRole == UserRole.ADMIN && currentRole != UserRole.ADMIN) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            pin = it
                            errorMessage = null
                        },
                        label = { Text("Admin Security PIN") },
                        placeholder = { Text("Default: 1234") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )
                    Text(
                        text = "Default Admin PIN is 1234",
                        fontSize = 11.sp,
                        color = Slate600,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = RoseDue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val success = onConfirmSwitch(targetRole, pin)
                    if (success) {
                        onDismiss()
                    } else {
                        errorMessage = "Incorrect PIN! Please enter 1234."
                    }
                },
                modifier = Modifier.testTag("confirm_role_switch")
            ) {
                Text("Confirm Switch", fontWeight = FontWeight.Bold)
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
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bg, fg, label) = when (status.uppercase()) {
        "PAID" -> Triple(EmeraldSuccessBg, EmeraldSuccess, "Paid")
        "PARTIAL" -> Triple(AmberPendingBg, AmberPending, "Partial")
        else -> Triple(RoseDueBg, RoseDue, "Unpaid")
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = fg,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
