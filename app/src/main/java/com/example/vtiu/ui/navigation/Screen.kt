 package com.example.vtiu.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object PortalSelection : Screen("portal_selection")
    object StudentLogin : Screen("student_login")
    object TeacherLogin : Screen("teacher_login")
    object ExamLogin : Screen("exam_login")
    object Dashboard : Screen("dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object TeacherClasses : Screen("teacher_classes")
    object TeacherAttendanceHub : Screen("teacher_attendance_hub/{courseId}") {
        fun createRoute(courseId: Int) = "teacher_attendance_hub/$courseId"
    }
    object TeacherAttendance : Screen("teacher_attendance/{courseId}") {
        fun createRoute(courseId: Int) = "teacher_attendance/$courseId"
    }
    object TeacherAttendanceAnalytics : Screen("teacher_attendance_analytics/{courseId}") {
        fun createRoute(courseId: Int) = "teacher_attendance_analytics/$courseId"
    }
    object TeacherGrading : Screen("teacher_grading")
    object TeacherProfile : Screen("teacher_profile")
    object TeacherAppointmentHub : Screen("teacher_appointment_hub")
    object TeacherManageSlots : Screen("teacher_manage_slots")
    object TeacherAppointmentRequests : Screen("teacher_appointment_requests")
    object TeacherQuizHub : Screen("teacher_quiz_hub")
    object TeacherManageQuizzes : Screen("teacher_manage_quizzes")
    object TeacherQuizSubmissions : Screen("teacher_quiz_submissions")
    object TeacherExamHub : Screen("teacher_exam_hub")
    object TeacherManageExams : Screen("teacher_manage_exams")
    object TeacherExamSubmissions : Screen("teacher_exam_submissions")
    object TeacherCreateQuiz : Screen("teacher_create_quiz?quizId={quizId}") {
        fun createRoute(quizId: Int? = null) = "teacher_create_quiz" + (quizId?.let { "?quizId=$it" } ?: "")
    }
    object TeacherCreateExam : Screen("teacher_create_exam")
    object TeacherAssignmentFileManager : Screen("teacher_assignment_file_manager")
    object TeacherMaterialsHub : Screen("teacher_materials_hub")
    object TeacherMaterialsUploader : Screen("teacher_materials_uploader")
    object TeacherManageMaterials : Screen("teacher_manage_materials")
    object TeacherAssessmentScheme : Screen("teacher_assessment_scheme")
    object TeacherClassPerformance : Screen("teacher_class_performance")
    object TeacherLiveHub : Screen("teacher_live_hub")
    object TeacherCreateLive : Screen("teacher_create_live")
    object TeacherLiveRoom : Screen("teacher_live_room/{meetingId}") {
        fun createRoute(meetingId: Int) = "teacher_live_room/$meetingId"
    }
    object AcademicCalendar : Screen("academic_calendar")
    object Courses : Screen("courses")
    object Profile : Screen("profile")
    object IdCard : Screen("id_card")
    object Results : Screen("results")
    object Fees : Screen("fees")
    object Assessments : Screen("assessments")
    object Timetable : Screen("timetable")
    object VClassDashboard : Screen("vclass_dashboard")
    object VClassMaterials : Screen("vclass_materials")
    object VClassAssignments : Screen("vclass_assignments")
    object VClassLive : Screen("vclass_live")
    object CourseRegistration : Screen("course_registration")
    object QuizInstructions : Screen("quiz_instructions/{quizId}") {
        fun createRoute(quizId: Int) = "quiz_instructions/$quizId"
    }
    object TakeQuiz : Screen("take_quiz/{quizId}") {
        fun createRoute(quizId: Int) = "take_quiz/$quizId"
    }
    object QuizResult : Screen("quiz_result/{title}/{score}/{total}") {
        fun createRoute(title: String, score: Float, total: Float) = "quiz_result/$title/$score/$total"
    }
    object SubmitAssignment : Screen("submit_assignment/{assignmentId}") {
        fun createRoute(assignmentId: Int) = "submit_assignment/$assignmentId"
    }
    object Notifications : Screen("notifications")
    object Transcript : Screen("transcript")
    object BookAppointment : Screen("book_appointment")
    object MyAppointments : Screen("my_appointments")
    object JoinMeeting : Screen("join_meeting/{meetingId}") {
        fun createRoute(meetingId: Int) = "join_meeting/$meetingId"
    }
    object LiveClassRoom : Screen("live_class_room/{meetingId}") {
        fun createRoute(meetingId: Int) = "live_class_room/$meetingId"
    }
    object VClassPlayer : Screen("vclass_player/{recordingId}") {
        fun createRoute(recordingId: Int) = "vclass_player/$recordingId"
    }
    object VClassPdfViewer : Screen("vclass_pdf_viewer/{materialId}") {
        fun createRoute(materialId: Int) = "vclass_pdf_viewer/$materialId"
    }
    object PayFees : Screen("pay_fees")
    object Exams : Screen("exams")
    object ExamInstructions : Screen("exam_instructions/{examId}") {
        fun createRoute(examId: Int) = "exam_instructions/$examId"
    }
    object TakeExam : Screen("take_exam/{examId}") {
        fun createRoute(examId: Int) = "take_exam/$examId"
    }
    object PaystackCheckout : Screen("paystack_checkout/{url}/{reference}") {
        fun createRoute(url: String, reference: String) = "paystack_checkout/${java.net.URLEncoder.encode(url, "UTF-8")}/$reference"
    }
    object Chat : Screen("chat")
}
