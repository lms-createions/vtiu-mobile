package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Assessment(
    val id: Int,
    val type: String, // Quiz, Assignment, Exam
    val course: String,
    val title: String,
    @SerialName("raw_score")
    val rawScore: Float,
    @SerialName("max_score")
    val maxScore: Float,
    val date: String?,
    val feedback: String?
) {
    val percentage: Float
        get() = if (maxScore > 0) (rawScore / maxScore) * 100f else 0f
}
