package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizOption(
    val id: Int,
    val text: String
)

@Serializable
data class QuizQuestion(
    val id: Int,
    @SerialName("question_text")
    val questionText: String,
    @SerialName("question_type")
    val questionType: String, // mcq, short_answer
    val marks: Float,
    val options: List<QuizOption> = emptyList()
)

@Serializable
data class QuizDetail(
    val id: Int,
    val title: String,
    @SerialName("course_name")
    val courseName: String,
    @SerialName("duration_minutes")
    val durationMinutes: Int,
    @SerialName("max_score")
    val maxScore: Float,
    @SerialName("start_datetime")
    val startDatetime: String,
    @SerialName("end_datetime")
    val endDatetime: String,
    @SerialName("attempts_allowed")
    val attemptsAllowed: Int,
    val questions: List<QuizQuestion> = emptyList()
)
