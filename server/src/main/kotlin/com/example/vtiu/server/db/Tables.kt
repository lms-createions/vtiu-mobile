package com.example.vtiu.server.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Admins : Table("admin") {
    val id = integer("id").autoIncrement()
    val publicId = varchar("public_id", 36).uniqueIndex()
    val adminId = varchar("admin_id", 100).uniqueIndex().nullable()
    val userId = varchar("user_id", 100).references(Users.userId).uniqueIndex().nullable()
    val username = varchar("username", 100).uniqueIndex()
    val email = varchar("email", 120).uniqueIndex().nullable()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 50).default("finance_admin")
    val isSuperadmin = bool("is_superadmin").default(false)
    val jobTitle = varchar("job_title", 100).nullable()
    val department = varchar("department", 100).nullable()
    val phone = varchar("phone", 20).nullable()
    val officeLocation = varchar("office_location", 100).nullable()
    val profilePicture = varchar("profile_picture", 255).default("default_avatar.png").nullable()
    
    // Permissions
    val canViewFinances = bool("can_view_finances").default(false)
    val canEditFinances = bool("can_edit_finances").default(false)
    val canApprovePayments = bool("can_approve_payments").default(false)
    val canManageFees = bool("can_manage_fees").default(false)
    val canViewAcademics = bool("can_view_academics").default(false)
    val canEditAcademics = bool("can_edit_academics").default(false)
    val canViewAdmissions = bool("can_view_admissions").default(false)
    val canEditAdmissions = bool("can_edit_admissions").default(false)
    val canManageUsers = bool("can_manage_users").default(false)
    val canViewReports = bool("can_view_reports").default(false)
    val canExportData = bool("can_export_data").default(false)

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val lastLogin = datetime("last_login").nullable()
    val dateAppointed = datetime("date_appointed").nullable()

    override val primaryKey = PrimaryKey(id)
}

object Users : Table("user") {
    val id = integer("id").autoIncrement()
    val publicId = varchar("public_id", 36).uniqueIndex()
    val userId = varchar("user_id", 20).uniqueIndex()
    val username = varchar("username", 100)
    val email = varchar("email", 120).uniqueIndex().nullable()
    val firstName = varchar("first_name", 100)
    val middleName = varchar("middle_name", 100).nullable()
    val lastName = varchar("last_name", 100)
    val role = varchar("role", 10)
    val passwordHash = varchar("password_hash", 200)
    val profilePicture = varchar("profile_picture", 255).default("default.png").nullable()
    val lastSeen = datetime("last_seen").nullable()

    override val primaryKey = PrimaryKey(id)
}

object StudentProfiles : Table("student_profile") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 20).references(Users.userId).uniqueIndex().nullable()
    val dob = varchar("dob", 50).nullable() // Stored as String for ease, adjust to date if needed
    val gender = varchar("gender", 10).nullable()
    val nationality = varchar("nationality", 50).nullable()
    val religion = varchar("religion", 50).nullable()
    val address = text("address").nullable()
    val city = varchar("city", 50).nullable()
    val state = varchar("state", 50).nullable()
    val postalCode = varchar("postal_code", 100).nullable()
    val phone = varchar("phone", 20).nullable()
    val email = varchar("email", 100).nullable()
    val currentProgramme = varchar("current_programme", 120)
    val programmeLevel = integer("programme_level")
    val studyFormat = varchar("study_format", 20).default("Regular")
    val lastLevelCompleted = integer("last_level_completed").nullable()
    val academicStatus = varchar("academic_status", 50).default("Active")
    val lastScore = float("last_score").nullable()
    val admissionDate = varchar("admission_date", 50).nullable()
    val indexNumber = varchar("index_number", 50).uniqueIndex().nullable()
    val semester = varchar("semester", 20).nullable()
    val academicYear = varchar("academic_year", 20).nullable()
    val guardianName = varchar("guardian_name", 120).nullable()
    val guardianRelation = varchar("guardian_relation", 50).nullable()
    val guardianPhone = varchar("guardian_phone", 20).nullable()
    val guardianEmail = varchar("guardian_email", 100).nullable()
    val guardianAddress = text("guardian_address").nullable()

    override val primaryKey = PrimaryKey(id)
}

object TeacherProfiles : Table("teacher_profile") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 20).references(Users.userId).uniqueIndex().nullable()
    val employeeId = varchar("employee_id", 20).uniqueIndex()
    val dob = varchar("dob", 50).nullable()
    val gender = varchar("gender", 10).nullable()
    val nationality = varchar("nationality", 50).nullable()
    val qualification = varchar("qualification", 100).nullable()
    val specialization = varchar("specialization", 100).nullable()
    val yearsOfExperience = integer("years_of_experience").nullable()
    val subjectsTaught = varchar("subjects_taught", 255).nullable()
    val employmentType = varchar("employment_type", 20).nullable()
    val department = varchar("department", 100).nullable()
    val dateOfHire = varchar("date_of_hire", 50).nullable()
    val officeLocation = varchar("office_location", 100).nullable()
    val dateJoined = varchar("date_joined", 50).nullable()

    override val primaryKey = PrimaryKey(id)
}

object Courses : Table("course") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val code = varchar("code", 20).uniqueIndex()
    val programmeName = varchar("programme_name", 120)
    val programmeLevel = varchar("programme_level", 20)
    val semester = varchar("semester", 10)
    val creditHours = integer("credit_hours").default(3)
    val academicYear = varchar("academic_year", 20)
    val isMandatory = bool("is_mandatory").default(false)
    val registrationStart = datetime("registration_start").nullable()
    val registrationEnd = datetime("registration_end").nullable()

    override val primaryKey = PrimaryKey(id)
}

object Assignments : Table("assignments") {
    val id = integer("id").autoIncrement()
    val courseName = varchar("course_name", 100)
    val courseId = integer("course_id").references(Courses.id)
    val title = varchar("title", 150)
    val description = text("description").nullable()
    val instructions = text("instructions").nullable()
    val programmeLevel = varchar("programme_level", 50)
    val programmeName = varchar("programme_name", 120).nullable()
    val dueDate = datetime("due_date")
    val filename = varchar("filename", 200).nullable()
    val originalName = varchar("original_name", 200).nullable()
    val createdAt = datetime("created_at")
    val maxScore = float("max_score")

    override val primaryKey = PrimaryKey(id)
}

object Quizzes : Table("quiz") {
    val id = integer("id").autoIncrement()
    val courseId = integer("course_id").references(Courses.id)
    val courseName = varchar("course_name", 100)
    val title = varchar("title", 255)
    val programmeLevel = varchar("programme_level", 50)
    val programmeName = varchar("programme_name", 120).nullable()
    val date = varchar("date", 50)
    val durationMinutes = integer("duration_minutes")
    val createdAt = datetime("created_at")
    val startDatetime = datetime("start_datetime")
    val endDatetime = datetime("end_datetime")
    val attemptsAllowed = integer("attempts_allowed").default(1)

    override val primaryKey = PrimaryKey(id)
}

object Exams : Table("exams") {
    val id = integer("id").autoIncrement()
    val courseId = integer("course_id").references(Courses.id)
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val programmeLevel = varchar("programme_level", 50)
    val programmeName = varchar("programme_name", 120).nullable()
    val durationMinutes = integer("duration_minutes").nullable()
    val startDatetime = datetime("start_datetime")
    val endDatetime = datetime("end_datetime")
    val createdAt = datetime("created_at")
    val assignmentMode = varchar("assignment_mode", 20).default("random")
    val assignmentSeed = varchar("assignment_seed", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

object StudentFeeTransactions : Table("student_fee_transaction") {
    val id = integer("id").autoIncrement()
    val studentId = integer("student_id").references(Users.id)
    val academicYear = varchar("academic_year", 20)
    val semester = varchar("semester", 10)
    val amount = float("amount")
    val description = varchar("description", 255)
    val timestamp = datetime("timestamp")
    val proofFilename = varchar("proof_filename", 255).nullable()
    val isApproved = bool("is_approved").default(false)
    val reviewedByAdminId = integer("reviewed_by_admin_id").references(Admins.id).nullable()

    override val primaryKey = PrimaryKey(id)
}

object StudentFeeBalances : Table("student_fee_balance") {
    val id = integer("id").autoIncrement()
    val studentId = varchar("student_id", 20).references(Users.userId)
    val feeStructureId = integer("fee_structure_id") // Should reference programme_fee_structure
    val programmeName = varchar("programme_name", 120)
    val programmeLevel = varchar("programme_level", 20)
    val studyFormat = varchar("study_format", 50).default("Regular")
    val academicYear = varchar("academic_year", 20)
    val semester = varchar("semester", 10)
    val amountDue = float("amount_due")
    val amountPaid = float("amount_paid").default(0.0f)
    val isPaid = bool("is_paid").default(false)
    val paidOn = datetime("paid_on").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}

object Notifications : Table("notifications") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 50)
    val title = varchar("title", 255)
    val message = text("message")
    val date = varchar("date", 50)
    val isRead = bool("is_read").default(false)

    override val primaryKey = PrimaryKey(id)
}

object AppointmentBookings : Table("appointment_booking") {
    val id = integer("id").autoIncrement()
    val studentId = integer("student_id").references(StudentProfiles.id)
    val slotId = integer("slot_id").references(AppointmentSlots.id)
    val status = varchar("status", 20).default("pending")
    val note = text("note").nullable()
    val requestedOn = datetime("requested_on")

    override val primaryKey = PrimaryKey(id)
}

object AppointmentSlots : Table("appointment_slot") {
    val id = integer("id").autoIncrement()
    val teacherId = integer("teacher_id").references(TeacherProfiles.id)
    val date = varchar("date", 50)
    val startTime = varchar("start_time", 20)
    val endTime = varchar("end_time", 20)
    val isBooked = bool("is_booked").default(false)

    override val primaryKey = PrimaryKey(id)
}

object AcademicCalendar : Table("academic_calendar") {
    val id = integer("id").autoIncrement()
    val date = varchar("date", 50).uniqueIndex()
    val label = varchar("label", 100)
    val breakType = varchar("break_type", 50)
    val isWorkday = bool("is_workday").default(false)

    override val primaryKey = PrimaryKey(id)
}

object TimetableEntries : Table("timetable_entry") {
    val id = integer("id").autoIncrement()
    val programmeName = varchar("programme_name", 120)
    val programmeLevel = varchar("programme_level", 20)
    val courseId = integer("course_id").references(Courses.id)
    val dayOfWeek = varchar("day_of_week", 10)
    val startTime = varchar("start_time", 20)
    val endTime = varchar("end_time", 20)

    override val primaryKey = PrimaryKey(id)
}

object StudentCourseGrades : Table("student_course_grade") {
    val id = integer("id").autoIncrement()
    val studentId = integer("student_id").references(Users.id)
    val courseId = integer("course_id").references(Courses.id)
    val academicYear = varchar("academic_year", 20)
    val semester = varchar("semester", 10)
    val finalScore = float("final_score").nullable()
    val gradeLetter = varchar("grade_letter", 5).nullable()
    val gradePoint = float("grade_point").nullable()
    val passFail = varchar("pass_fail", 10).nullable()
    val lastUpdated = datetime("last_updated")

    override val primaryKey = PrimaryKey(id)
}

object TeacherCourseAssignments : Table("teacher_course_assignment") {
    val id = integer("id").autoIncrement()
    val teacherId = integer("teacher_id").references(TeacherProfiles.id)
    val courseId = integer("course_id").references(Courses.id)

    override val primaryKey = PrimaryKey(id)
}

object StudentCourseRegistrations : Table("student_course_registration") {
    val id = integer("id").autoIncrement()
    val studentId = integer("student_id").references(Users.id)
    val courseId = integer("course_id").references(Courses.id)
    val academicYear = varchar("academic_year", 20)
    val semester = varchar("semester", 10)

    override val primaryKey = PrimaryKey(id)
}

object AttendanceRecords : Table("attendance_record") {
    val id = integer("id").autoIncrement()
    val studentId = varchar("student_id", 20).references(Users.userId)
    val teacherId = integer("teacher_id").references(TeacherProfiles.id)
    val courseId = integer("course_id").references(Courses.id).nullable()
    val date = varchar("date", 50)
    val isPresent = bool("is_present").default(false)

    override val primaryKey = PrimaryKey(id)
}

object CourseAssessmentSchemes : Table("course_assessment_scheme") {
    val id = integer("id").autoIncrement()
    val courseId = integer("course_id").references(Courses.id)
    val teacherId = integer("teacher_id").references(TeacherProfiles.id)
    val quizWeight = float("quiz_weight").default(10.0f)
    val assignmentWeight = float("assignment_weight").default(30.0f)
    val examWeight = float("exam_weight").default(60.0f)

    override val primaryKey = PrimaryKey(id)
}

object AssignmentSubmissions : Table("assignment_submissions") {
    val id = integer("id").autoIncrement()
    val assignmentId = integer("assignment_id").references(Assignments.id)
    val studentId = integer("student_id").references(Users.id)
    val filename = varchar("filename", 255)
    val originalName = varchar("original_name", 255)
    val submittedAt = datetime("submitted_at")
    val score = float("score").nullable()
    val feedback = text("feedback").nullable()
    val scoredAt = datetime("scored_at").nullable()
    val gradeLetter = varchar("grade_letter", 5).nullable()
    val passFail = varchar("pass_fail", 10).nullable()

    override val primaryKey = PrimaryKey(id)
}

object Meetings : Table("meetings") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 200)
    val description = text("description").nullable()
    val hostId = integer("host_id").references(Users.id)
    val meetingCode = varchar("meeting_code", 80).uniqueIndex()
    val courseId = integer("course_id").references(Courses.id)
    val scheduledStart = datetime("scheduled_start").nullable()
    val scheduledEnd = datetime("scheduled_end").nullable()
    val joinUrl = varchar("join_url", 500).nullable()
    val startUrl = varchar("start_url", 500).nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

object Questions : Table("question") {
    val id = integer("id").autoIncrement()
    val quizId = integer("quiz_id").references(Quizzes.id)
    val text = text("text")
    val points = float("points").default(1.0f)
    val questionType = varchar("question_type", 50).default("mcq")

    override val primaryKey = PrimaryKey(id)
}

object Options : Table("options") {
    val id = integer("id").autoIncrement()
    val questionId = integer("question_id").references(Questions.id)
    val text = varchar("text", 1000)
    val isCorrect = bool("is_correct").default(false)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

object StudentQuizSubmissions : Table("student_quiz_submissions") {
    val id = integer("id").autoIncrement()
    val studentId = integer("student_id").references(Users.id)
    val quizId = integer("quiz_id").references(Quizzes.id)
    val score = float("score").nullable()
    val submittedAt = datetime("submitted_at")

    override val primaryKey = PrimaryKey(id)
}

object CourseMaterials : Table("course_material") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 120)
    val programmeName = varchar("programme_name", 100)
    val programmeLevel = varchar("programme_level", 50)
    val courseName = varchar("course_name", 100)
    val filename = varchar("filename", 200)
    val originalName = varchar("original_name", 200)
    val fileType = varchar("file_type", 20)
    val uploadDate = datetime("upload_date")

    override val primaryKey = PrimaryKey(id)
}
