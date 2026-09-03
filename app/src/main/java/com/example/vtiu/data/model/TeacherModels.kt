package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeacherProfile(
    val id: Int,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val department: String,
    val email: String,
    @SerialName("profile_picture")
    val profilePicture: String? = null
)

@Serializable
data class TeacherClass(
    val id: Int,
    @SerialName("course_name")
    val courseName: String,
    @SerialName("course_code")
    val courseCode: String,
    @SerialName("programme_name")
    val programmeName: String,
    @SerialName("programme_level")
    val programmeLevel: String,
    @SerialName("student_count")
    val studentCount: Int
)

@Serializable
data class AssignmentSubmissionPreview(
    val id: Int,
    @SerialName("student_name")
    val studentName: String,
    @SerialName("student_id")
    val studentId: String,
    @SerialName("assignment_title")
    val assignmentTitle: String,
    @SerialName("submitted_at")
    val submittedAt: String,
    val status: String // Pending, Graded
)

@Serializable
data class AttendanceStat(
    val studentName: String,
    val percentage: Int,
    val totalSessions: Int,
    val presentCount: Int,
    val absentCount: Int
)

@Serializable
data class CourseAssessmentScheme(
    val id: Int,
    @SerialName("course_id")
    val courseId: Int,
    @SerialName("course_name")
    val courseName: String,
    @SerialName("course_code")
    val courseCode: String,
    @SerialName("quiz_weight")
    var quizWeight: Float = 10f,
    @SerialName("assignment_weight")
    var assignmentWeight: Float = 30f,
    @SerialName("exam_weight")
    var examWeight: Float = 60f
)

@Serializable
data class ClassPerformanceRow(
    val studentId: String,
    val studentName: String,
    val quizScore: Float, // Weighted
    val assignmentScore: Float, // Weighted
    val examScore: Float, // Weighted
    val totalScore: Float,
    val grade: String,
    val status: String // Passed, Failed
)
