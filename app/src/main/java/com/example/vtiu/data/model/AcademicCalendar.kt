package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicEvent(
    val id: Int,
    val title: String,
    val date: String, // YYYY-MM-DD
    @SerialName("break_type")
    val type: String, // Lecture, Exam, Holiday, Vacation, etc.
    @SerialName("is_workday")
    val isWorkday: Boolean = true,
    val description: String? = null
)
