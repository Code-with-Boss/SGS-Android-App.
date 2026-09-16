package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.data.model.SchoolBranding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object BrandingManager {

    private const val PREFS_NAME = "school_branding_prefs"
    private const val KEY_SCHOOL_LOGO_PATH = "key_school_logo_path"
    private const val KEY_APP_ICON_PATH = "key_app_icon_path"
    private const val KEY_SCHOOL_NAME = "key_school_name"
    private const val KEY_SCHOOL_TAGLINE = "key_school_tagline"
    private const val KEY_LAST_UPDATED = "key_last_updated"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getBranding(context: Context): SchoolBranding {
        val prefs = getPrefs(context)
        val logoPath = prefs.getString(KEY_SCHOOL_LOGO_PATH, null)
        val iconPath = prefs.getString(KEY_APP_ICON_PATH, null)

        val validLogoPath = if (logoPath != null && File(logoPath).exists()) logoPath else null
        val validIconPath = if (iconPath != null && File(iconPath).exists()) iconPath else null

        return SchoolBranding(
            schoolLogoPath = validLogoPath,
            appIconPath = validIconPath,
            schoolName = prefs.getString(KEY_SCHOOL_NAME, "Scholar Grammar High School") ?: "Scholar Grammar High School",
            schoolTagline = prefs.getString(KEY_SCHOOL_TAGLINE, "Govt. Reg. Quality Education • Character Building") ?: "Govt. Reg. Quality Education • Character Building",
            lastUpdated = prefs.getLong(KEY_LAST_UPDATED, System.currentTimeMillis())
        )
    }

    fun saveImageFromUri(context: Context, uri: Uri, isAppIcon: Boolean): String? {
        return try {
            val fileName = if (isAppIcon) "app_icon.png" else "school_logo.png"
            val destFile = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Compress & normalize as PNG to ensure reliable loading and reasonable file size
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    FileOutputStream(destFile).use { outStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 95, outStream)
                    }
                } else {
                    // Fallback to raw copy
                    FileOutputStream(destFile).use { outStream ->
                        inputStream.copyTo(outStream)
                    }
                }
            }

            val savedPath = destFile.absolutePath
            val now = System.currentTimeMillis()
            getPrefs(context).edit().apply {
                if (isAppIcon) {
                    putString(KEY_APP_ICON_PATH, savedPath)
                } else {
                    putString(KEY_SCHOOL_LOGO_PATH, savedPath)
                }
                putLong(KEY_LAST_UPDATED, now)
                apply()
            }
            savedPath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun resetLogo(context: Context, isAppIcon: Boolean) {
        val fileName = if (isAppIcon) "app_icon.png" else "school_logo.png"
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }

        getPrefs(context).edit().apply {
            if (isAppIcon) {
                remove(KEY_APP_ICON_PATH)
            } else {
                remove(KEY_SCHOOL_LOGO_PATH)
            }
            putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
            apply()
        }
    }

    fun updateSchoolInfo(context: Context, name: String, tagline: String) {
        getPrefs(context).edit().apply {
            putString(KEY_SCHOOL_NAME, name.trim())
            putString(KEY_SCHOOL_TAGLINE, tagline.trim())
            putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
            apply()
        }
    }

    fun getLogoBase64(context: Context): String? {
        val branding = getBranding(context)
        val path = branding.schoolLogoPath ?: return null
        val file = File(path)
        if (!file.exists()) return null

        return try {
            val bytes = file.readBytes()
            "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}
