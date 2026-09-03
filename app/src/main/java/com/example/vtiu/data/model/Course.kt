package com.example.vtiu.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Course(
    val id: Int,
    val name: String,
    val code: String,
    @SerialName("programme_name")
    val programmeName: String,
    @SerialName("programme_level")
    val programmeLevel: String,
    val semester: String,
    @SerialName("credit_hours")
    val creditHours: Int,
    @SerialName("is_mandatory")
    val isMandatory: Boolean = true
)
