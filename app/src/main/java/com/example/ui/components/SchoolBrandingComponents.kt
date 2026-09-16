package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AppSettingsAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.SchoolBranding
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessBg
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
import java.io.File

@Composable
fun SchoolLogoView(
    branding: SchoolBranding,
    modifier: Modifier = Modifier,
    isAppIcon: Boolean = false,
    shape: Shape = CircleShape,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val targetPath = if (isAppIcon) {
        branding.appIconPath ?: branding.schoolLogoPath
    } else {
        branding.schoolLogoPath
    }

    val customFile = targetPath?.let { File(it) }

    Surface(
        shape = shape,
        color = Color.White.copy(alpha = 0.2f),
        modifier = modifier
    ) {
        if (customFile != null && customFile.exists()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(customFile)
                    .crossfade(true)
                    .setParameter("timestamp", branding.lastUpdated, memoryCacheKey = "${customFile.absolutePath}_${branding.lastUpdated}")
                    .build(),
                contentDescription = if (isAppIcon) "App Icon" else "School Logo",
                contentScale = contentScale,
                modifier = Modifier
                    .clip(shape)
                    .testTag(if (isAppIcon) "branding_app_icon" else "branding_school_logo")
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.school_logo_icon),
                contentDescription = if (isAppIcon) "Default App Icon" else "Default School Logo",
                contentScale = contentScale,
                modifier = Modifier
                    .clip(shape)
                    .testTag(if (isAppIcon) "branding_app_icon_default" else "branding_school_logo_default")
            )
        }
    }
}

@Composable
fun AdminBrandingDialog(
    branding: SchoolBranding,
    onDismiss: () -> Unit,
    onUpdateLogo: (Uri) -> Unit,
    onUpdateAppIcon: (Uri) -> Unit,
    onResetLogo: () -> Unit,
    onResetAppIcon: () -> Unit,
    onUpdateSchoolInfo: (String, String) -> Unit
) {
    var schoolName by remember(branding.schoolName) { mutableStateOf(branding.schoolName) }
    var schoolTagline by remember(branding.schoolTagline) { mutableStateOf(branding.schoolTagline) }
    var isEditingDetails by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdateLogo(uri)
        }
    }

    val iconPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdateAppIcon(uri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SchoolNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = SchoolGoldLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "School Branding & Logos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SchoolNavy
                        )
                        Text(
                            text = "سکول کا لوگو اور ایپ آئیکون ترتیبات",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate600)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Info banner about Local Storage
                Surface(
                    color = EmeraldSuccessBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Saved in App Local Storage (لوکل اسٹوریج)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                            Text(
                                text = "نئے منتخب کردہ لوگو ایپ کی انٹرنل میموری میں محفوظ رہیں گے اور ہر چالان پر آئیں گے۔",
                                fontSize = 11.sp,
                                color = Slate700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 1: School Logo (Used in Header and Invoices)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "1. School Official Logo (سکول لوگو)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SchoolNavy
                                )
                                Text(
                                    text = "ڈیش بورڈ، ہیڈر اور تمام فیس چالان / انوائس پر آئے گا",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }

                            if (branding.schoolLogoPath != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Custom Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(2.dp, SchoolNavy, CircleShape)
                                    .padding(2.dp)
                            ) {
                                SchoolLogoView(
                                    branding = branding,
                                    isAppIcon = false,
                                    modifier = Modifier.size(68.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = {
                                        logoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pick_school_logo_button")
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("گیلری سے لوگو چنیں", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                }

                                if (branding.schoolLogoPath != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = onResetLogo,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("اصل لوگو بحال کریں", fontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 2: App Branding Icon
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "2. App Branding Icon (ایپ آئیکون)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SchoolNavy
                                )
                                Text(
                                    text = "ایپ کے ٹاپ بار اور اندرونی تھیم کیلئے",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }

                            if (branding.appIconPath != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Custom Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(2.dp, SchoolBlue, RoundedCornerShape(16.dp))
                                    .padding(2.dp)
                            ) {
                                SchoolLogoView(
                                    branding = branding,
                                    isAppIcon = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.size(68.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = {
                                        iconPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SchoolBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pick_app_icon_button")
                                ) {
                                    Icon(Icons.Default.AppSettingsAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("گیلری سے آئیکون چنیں", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                }

                                if (branding.appIconPath != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = onResetAppIcon,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("اصل آئیکون بحال کریں", fontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 3: School Name & Tagline Editor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. School Title & Tagline (نام اور سلوگن)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = SchoolNavy
                            )
                            IconButton(onClick = { isEditingDetails = !isEditingDetails }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit details", modifier = Modifier.size(18.dp), tint = SchoolNavy)
                            }
                        }

                        if (isEditingDetails) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = schoolName,
                                onValueChange = { schoolName = it },
                                label = { Text("School Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = schoolTagline,
                                onValueChange = { schoolTagline = it },
                                label = { Text("Tagline / Govt Reg Info") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onUpdateSchoolInfo(schoolName, schoolTagline)
                                    isEditingDetails = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save Title Details")
                            }
                        } else {
                            Text(
                                text = branding.schoolName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Slate800
                            )
                            Text(
                                text = branding.schoolTagline,
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dismiss_branding_dialog_button")
            ) {
                Text("Done (مکمل)")
            }
        }
    )
}
