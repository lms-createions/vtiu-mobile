package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseResult(
    @SerialName("course_code")
    val courseCode: String,
    @SerialName("course_name")
    val courseName: String,
    val score: Float,
    val grade: String,
    @SerialName("credit_hours")
    val creditHours: Int,
    val points: Float,
    @SerialName("quiz_weight")
    val quizWeight: Float,
    @SerialName("assignment_weight")
    val assignmentWeight: Float,
    @SerialName("exam_weight")
    val examWeight: Float,
    val remark: String
)

@Serializable
data class SemesterResults(
    @SerialName("academic_year")
    val academicYear: String,
    val semester: String,
    val results: List<CourseResult>
)
