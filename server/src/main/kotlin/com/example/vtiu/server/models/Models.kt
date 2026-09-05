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

// Teacher Request/Response Models
@Serializable
data class MarkAttendanceRequest(
    @SerialName("student_id") val studentId: String,
    @SerialName("course_id") val courseId: Int,
    val date: String,
    @SerialName("is_present") val isPresent: Boolean
)

@Serializable
data class AttendanceRecordApi(
    val id: Int,
    @SerialName("student_name") val studentName: String,
    @SerialName("student_id") val studentId: String,
    val date: String,
    @SerialName("is_present") val isPresent: Boolean
)

@Serializable
data class AttendanceAnalyticsApi(
    @SerialName("student_name") val studentName: String,
    @SerialName("total_classes") val totalClasses: Int,
    @SerialName("attended_count") val attendedCount: Int,
    @SerialName("attendance_percentage") val attendancePercentage: Float
)

@Serializable
data class GradeSubmissionRequest(
    @SerialName("submission_id") val submissionId: Int,
    val score: Float,
    val feedback: String? = null
)

@Serializable
data class AssignmentSubmissionApi(
    val id: Int,
    @SerialName("student_name") val studentName: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("assignment_title") val assignmentTitle: String,
    @SerialName("submitted_at") val submittedAt: String,
    val filename: String,
    val score: Float? = null,
    val feedback: String? = null
)

@Serializable
data class TeacherSlotApi(
    val id: Int,
    val date: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("is_booked") val isBooked: Boolean
)

@Serializable
data class TeacherBookingApi(
    val id: Int,
    @SerialName("student_name") val studentName: String,
    val date: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val status: String,
    val note: String?
)

@Serializable
data class QuizSubmissionApi(
    val id: Int,
    @SerialName("student_name") val studentName: String,
    @SerialName("quiz_title") val quizTitle: String,
    val score: Float?,
    @SerialName("submitted_at") val submittedAt: String
)

@Serializable
data class ExamSubmissionApi(
    val id: Int,
    @SerialName("student_name") val studentName: String,
    @SerialName("exam_title") val examTitle: String,
    val score: Float?,
    @SerialName("submitted_at") val submittedAt: String
)

@Serializable
data class CourseAssessmentSchemeApi(
    val id: Int,
    @SerialName("course_id") val courseId: Int,
    @SerialName("course_name") val courseName: String,
    @SerialName("quiz_weight") val quizWeight: Float,
    @SerialName("assignment_weight") val assignmentWeight: Float,
    @SerialName("exam_weight") val examWeight: Float
)

@Serializable
data class CreateMeetingRequest(
    val title: String,
    val description: String?,
    @SerialName("course_id") val courseId: Int,
    @SerialName("scheduled_start") val scheduledStart: String,
    @SerialName("scheduled_end") val scheduledEnd: String
)

@Serializable
data class QuizCreateRequest(
    val title: String,
    @SerialName("course_id") val courseId: Int,
    @SerialName("programme_name") val programmeName: String,
    @SerialName("programme_level") val programmeLevel: String,
    @SerialName("start_datetime") val startDatetime: String,
    @SerialName("end_datetime") val endDatetime: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
    val questions: List<QuizQuestionCreateRequest>
)

@Serializable
data class QuizQuestionCreateRequest(
    val text: String,
    val type: String,
    val points: Float,
    val options: List<QuizOptionCreateRequest>
)

@Serializable
data class QuizOptionCreateRequest(
    val text: String,
    @SerialName("is_correct") val isCorrect: Boolean
)

@Serializable
data class ExamCreateRequest(
    val title: String,
    @SerialName("course_id") val courseId: Int,
    @SerialName("programme_name") val programmeName: String,
    @SerialName("programme_level") val programmeLevel: String,
    @SerialName("start_datetime") val startDatetime: String,
    @SerialName("end_datetime") val endDatetime: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("assignment_mode") val assignmentMode: String = "random"
)

@Serializable
data class ClassPerformanceApi(
    @SerialName("student_name") val studentName: String,
    @SerialName("quiz_avg") val quizAvg: Float,
    @SerialName("assignment_avg") val assignmentAvg: Float,
    @SerialName("exam_score") val examScore: Float?,
    @SerialName("final_grade") val finalGrade: String?
)

@Serializable
data class UploadMaterialRequest(
    val title: String,
    @SerialName("programme_name") val programmeName: String,
    @SerialName("programme_level") val programmeLevel: String,
    @SerialName("course_name") val courseName: String,
    val filename: String,
    @SerialName("file_type") val fileType: String
)

@Serializable
data class CourseRegistrationApi(
    val id: Int,
    val name: String,
    val code: String,
    val credits: Int,
    @SerialName("is_mandatory") val isMandatory: Boolean,
    val semester: String,
    @SerialName("academic_year") val academicYear: String
)

@Serializable
data class RegisterCoursesRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("course_ids") val courseIds: List<Int>,
    val semester: String,
    @SerialName("academic_year") val academicYear: String
)

@Serializable
data class StudentAssessmentApi(
    val type: String, // Quiz, Assignment, Exam
    val course: String,
    val title: String,
    @SerialName("raw_score") val rawScore: Float,
    @SerialName("max_score") val maxScore: Float,
    val date: String,
    val feedback: String? = null
)

@Serializable
data class TimetableEntryApi(
    val id: Int,
    @SerialName("course_name") val courseName: String,
    @SerialName("day_of_week") val dayOfWeek: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String
)

@Serializable
data class PayFeesRequest(
    @SerialName("user_id") val userId: String,
    val amount: Double,
    val description: String,
    val semester: String,
    @SerialName("academic_year") val academicYear: String
)

@Serializable
data class FeeTransactionApi(
    val id: Int,
    val amount: Double,
    val description: String,
    val date: String,
    val status: String, // Approved, Pending
    @SerialName("academic_year") val academicYear: String,
    val semester: String
)

@Serializable
data class FeeBalanceApi(
    val balance: Double,
    val paid: Double,
    val total: Double
)

@Serializable
data class AppointmentSlotApi(
    val id: Int,
    @SerialName("teacher_name") val teacherName: String,
    val date: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String
)

@Serializable
data class AppointmentBookingApi(
    val id: Int,
    @SerialName("teacher_name") val teacherName: String,
    val title: String,
    val date: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val status: String
)

@Serializable
data class NotificationApi(
    val id: Int,
    val title: String,
    val message: String,
    val date: String,
    @SerialName("is_read") val isRead: Boolean
)

@Serializable
data class VClassMeetingApi(
    val id: Int,
    val title: String,
    val description: String?,
    @SerialName("course_name") val courseName: String,
    @SerialName("scheduled_start") val scheduledStart: String,
    @SerialName("scheduled_end") val scheduledEnd: String,
    @SerialName("join_url") val joinUrl: String?,
    @SerialName("meeting_code") val meetingCode: String,
    val status: String = "Upcoming"
)

@Serializable
data class QuizDetailApi(
    val id: Int,
    val title: String,
    @SerialName("course_name") val courseName: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("max_score") val maxScore: Float,
    @SerialName("start_datetime") val startDatetime: String,
    @SerialName("end_datetime") val endDatetime: String,
    @SerialName("attempts_allowed") val attemptsAllowed: Int,
    val questions: List<QuizQuestionApi> = emptyList()
)

@Serializable
data class QuizQuestionApi(
    val id: Int,
    @SerialName("text") val questionText: String,
    @SerialName("type") val questionType: String,
    val points: Float,
    val options: List<QuizOptionApi> = emptyList()
)

@Serializable
data class QuizOptionApi(
    val id: Int,
    val text: String,
    @SerialName("is_correct") val isCorrect: Boolean = false
)

@Serializable
data class TranscriptApi(
    @SerialName("student_id") val studentId: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("academic_year") val academicYear: String? = null,
    val semester: String? = null,
    @SerialName("is_released") val isReleased: Boolean = true,
    val gpa: Float,
    @SerialName("weighted_gpa") val weightedGpa: Float? = null,
    @SerialName("cumulative_gpa") val cumulativeGpa: Float? = null,
    @SerialName("total_credits") val totalCredits: Int,
    @SerialName("total_credits_attempted") val totalCreditsAttempted: Int? = null,
    @SerialName("total_credits_earned") val totalCreditsEarned: Int? = null,
    val courses: List<TranscriptCourseApi> = emptyList(),
    val semesters: List<TranscriptSemesterApi> = emptyList()
)

@Serializable
data class TranscriptCourseApi(
    val code: String,
    val name: String,
    val credits: Int,
    val score: Float?,
    val grade: String?,
    @SerialName("quiz_score") val quizScore: Float? = null,
    @SerialName("assignment_score") val assignmentScore: Float? = null,
    @SerialName("exam_score") val examScore: Float? = null
)

@Serializable
data class SemesterResultApi(
    @SerialName("academic_year") val academicYear: String,
    val semester: String,
    @SerialName("is_released") val isReleased: Boolean,
    @SerialName("semester_gpa") val gpa: Float,
    @SerialName("total_credits") val totalCredits: Int,
    val results: List<TranscriptCourseApi>
)

@Serializable
data class AcademicSummaryApi(
    @SerialName("cumulative_gpa") val cumulativeGpa: Float,
    @SerialName("total_credits_earned") val totalCreditsEarned: Int,
    @SerialName("academic_status") val academicStatus: String,
    @SerialName("current_level") val currentLevel: Int
)

@Serializable
data class TranscriptSemesterApi(
    @SerialName("academic_year") val academicYear: String,
    val semester: String,
    val gpa: Float,
    val courses: List<TranscriptCourseApi>
)
