package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TranscriptCourse(
    @SerialName("course_code")
    val courseCode: String,
    @SerialName("course_name")
    val courseName: String,
    @SerialName("credit_hours")
    val creditHours: Int,
    @SerialName("final_score")
    val finalScore: Float,
    @SerialName("grade_letter")
    val gradeLetter: String?,
    @SerialName("quiz_weight")
    val quizWeight: Float,
    @SerialName("assignment_weight")
    val assignmentWeight: Float,
    @SerialName("exam_weight")
    val examWeight: Float
)

@Serializable
data class SemesterTranscript(
    @SerialName("academic_year")
    val academicYear: String,
    val semester: String,
    @SerialName("courses_count")
    val coursesCount: Int,
    val gpa: Float,
    @SerialName("weighted_gpa")
    val weightedGpa: Float,
    @SerialName("credit_hours")
    val creditHours: Int,
    @SerialName("is_released")
    val isReleased: Boolean,
    val courses: List<TranscriptCourse> = emptyList()
)

@Serializable
data class FullTranscript(
    @SerialName("student_id")
    val studentId: String,
    @SerialName("student_name")
    val studentName: String,
    @SerialName("cumulative_gpa")
    val cumulativeGpa: Float,
    @SerialName("cumulative_weighted_gpa")
    val cumulativeWeightedGpa: Float,
    @SerialName("total_credit_hours_attempted")
    val totalCreditHoursAttempted: Int,
    @SerialName("total_credit_hours_earned")
    val totalCreditHoursEarned: Int,
    val semesters: List<SemesterTranscript>
)
