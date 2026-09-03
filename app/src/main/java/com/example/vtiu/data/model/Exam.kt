package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExamOption(
    val id: Int,
    val text: String
)

@Serializable
data class ExamQuestion(
    val id: Int,
    @SerialName("question_text")
    val questionText: String,
    val marks: Float,
    val options: List<ExamOption> = emptyList()
)

@Serializable
data class ExamDetail(
    val id: Int,
    val title: String,
    @SerialName("course_name")
    val courseName: String,
    @SerialName("duration_minutes")
    val durationMinutes: Int,
    @SerialName("start_datetime")
    val startDatetime: String,
    @SerialName("end_datetime")
    val endDatetime: String,
    @SerialName("pass_percent")
    val passPercent: Float = 0.5f,
    val status: String = "Upcoming", // Upcoming, Ongoing, Ended
    val questions: List<ExamQuestion> = emptyList()
)
