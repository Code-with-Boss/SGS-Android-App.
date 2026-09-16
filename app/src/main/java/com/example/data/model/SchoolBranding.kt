package com.example.data.model

data class SchoolBranding(
    val schoolLogoPath: String? = null,
    val appIconPath: String? = null,
    val schoolName: String = "Scholar Grammar High School",
    val schoolTagline: String = "Govt. Reg. Quality Education • Character Building",
    val lastUpdated: Long = System.currentTimeMillis()
)
