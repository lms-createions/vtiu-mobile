package com.example.vtiu.server.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginRequest(
    val username: String,
    @SerialName("user_id")
    val userId: String,
    val password: String,
    val role: String = "teacher"
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val user: UserData? = null
)

@Serializable
data class UserData(
    val id: Int,
    @SerialName("user_id") val userId: String,
    val name: String,
    val role: String,
    val department: String? = null,
    @SerialName("profile_picture_url") val profilePictureUrl: String? = null
)

@Serializable
data class ProfileResponse(
    val success: Boolean,
    val profile: UserProfileData? = null,
    val message: String? = null
)

@Serializable
data class UserProfileData(
    @SerialName("user_id") val userId: String,
    val username: String,
    val name: String,
    val email: String?,
    val role: String,
    val department: String? = null,
    @SerialName("employee_id") val employeeId: String? = null,
    val qualification: String? = null,
    val specialization: String? = null,
    @SerialName("office_location") val officeLocation: String? = null,
    val programme: String? = null,
    val level: Int? = null,
    @SerialName("index_number") val indexNumber: String? = null,
    @SerialName("profile_picture_url") val profilePictureUrl: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val nationality: String? = null,
    val religion: String? = null,
    val phone: String? = null,
    val address: String? = null,
    @SerialName("academic_year") val academicYear: String? = null,
    val semester: String? = null,
    @SerialName("academic_status") val academicStatus: String? = null
)

@Serializable
data class TeacherClassApi(
    val id: Int,
    @SerialName("course_name") val courseName: String,
    @SerialName("course_code") val courseCode: String,
    val programme: String,
    val level: String,
    @SerialName("student_count") val studentCount: Int = 0
)

@Serializable
data class StudentCourseApi(
    val id: Int,
    val name: String,
    val code: String,
    val level: String,
    val credits: Int
)

@Serializable
data class CalendarEventApi(
    val id: Int,
    val title: String,
    val date: String,
    val type: String,
    @SerialName("is_workday") val isWorkday: Boolean
)

@Serializable
data class MaterialApi(
    val id: Int,
    val title: String,
    @SerialName("course_name") val courseName: String,
    @SerialName("file_url") val fileUrl: String,
    @SerialName("file_type") val fileType: String,
    @SerialName("upload_date") val uploadDate: String
)

@Serializable
data class VClassAssignmentApi(
    val id: Int,
    val title: String,
    @SerialName("course_name") val courseName: String,
    @SerialName("due_date") val dueDate: String,
    @SerialName("max_score") val maxScore: Float,
    val status: String = "Pending"
)
