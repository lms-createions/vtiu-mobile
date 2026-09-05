package com.example.vtiu.server.routes

import com.example.vtiu.server.models.*
import com.example.vtiu.server.db.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun Route.studentRoutes() {
    route("/api") {
        get("/student/courses/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val courses = transaction {
                val userRow = Users.select { Users.userId eq userId }.singleOrNull() ?: return@transaction emptyList<StudentCourseApi>()
                (StudentCourseRegistrations innerJoin Courses).select { StudentCourseRegistrations.studentId eq userRow[Users.id] }.map {
                    StudentCourseApi(
                        id = it[Courses.id],
                        name = it[Courses.name],
                        code = it[Courses.code],
                        level = it[Courses.programmeLevel],
                        credits = it[Courses.creditHours]
                    )
                }
            }
            call.respond(courses)
        }

        get("/student/registration/available/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val available = transaction {
                val studentProfile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<CourseRegistrationApi>()
                val programme = studentProfile[StudentProfiles.currentProgramme]
                val level = studentProfile[StudentProfiles.programmeLevel].toString()
                
                Courses.select { (Courses.programmeName eq programme) and (Courses.programmeLevel eq level) }.map {
                    CourseRegistrationApi(
                        id = it[Courses.id],
                        name = it[Courses.name],
                        code = it[Courses.code],
                        credits = it[Courses.creditHours],
                        isMandatory = it[Courses.isMandatory],
                        semester = it[Courses.semester],
                        academicYear = it[Courses.academicYear]
                    )
                }
            }
            call.respond(available)
        }

        post("/student/register") {
            val request = call.receive<RegisterCoursesRequest>()
            val success = transaction {
                val userRow = Users.select { Users.userId eq request.userId }.singleOrNull() ?: return@transaction false
                // Remove old registrations for this sem/year
                StudentCourseRegistrations.deleteWhere { 
                    (StudentCourseRegistrations.studentId eq userRow[Users.id]) and 
                    (StudentCourseRegistrations.semester eq request.semester) and 
                    (StudentCourseRegistrations.academicYear eq request.academicYear) 
                }
                // Add new ones
                request.courseIds.forEach { cid ->
                    StudentCourseRegistrations.insert {
                        it[StudentCourseRegistrations.studentId] = userRow[Users.id]
                        it[StudentCourseRegistrations.courseId] = cid
                        it[StudentCourseRegistrations.semester] = request.semester
                        it[StudentCourseRegistrations.academicYear] = request.academicYear
                    }
                }
                true
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.BadRequest)
        }

        get("/student/assessments/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val assessments = transaction {
                val userRow = Users.select { Users.userId eq userId }.singleOrNull() ?: return@transaction emptyList<StudentAssessmentApi>()
                val list = mutableListOf<StudentAssessmentApi>()
                
                // Quizzes
                (StudentQuizSubmissions innerJoin Quizzes).select { StudentQuizSubmissions.studentId eq userRow[Users.id] }.forEach {
                    list.add(StudentAssessmentApi(
                        type = "Quiz",
                        course = it[Quizzes.courseName],
                        title = it[Quizzes.title],
                        rawScore = it[StudentQuizSubmissions.score] ?: 0f,
                        maxScore = 0f, // Simplified
                        date = it[StudentQuizSubmissions.submittedAt].toString()
                    ))
                }
                
                // Assignments
                (AssignmentSubmissions innerJoin Assignments).select { AssignmentSubmissions.studentId eq userRow[Users.id] }.forEach {
                    list.add(StudentAssessmentApi(
                        type = "Assignment",
                        course = it[Assignments.courseName],
                        title = it[Assignments.title],
                        rawScore = it[AssignmentSubmissions.score] ?: 0f,
                        maxScore = it[Assignments.maxScore],
                        date = it[AssignmentSubmissions.submittedAt].toString(),
                        feedback = it[AssignmentSubmissions.feedback]
                    ))
                }
                list
            }
            call.respond(assessments)
        }

        get("/student/timetable/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val entries = transaction {
                val studentProfile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<TimetableEntryApi>()
                val level = studentProfile[StudentProfiles.programmeLevel].toString()
                
                (TimetableEntries innerJoin Courses).select { TimetableEntries.programmeLevel eq level }.map {
                    TimetableEntryApi(
                        id = it[TimetableEntries.id],
                        courseName = it[Courses.name],
                        dayOfWeek = it[TimetableEntries.dayOfWeek],
                        startTime = it[TimetableEntries.startTime],
                        endTime = it[TimetableEntries.endTime]
                    )
                }
            }
            call.respond(entries)
        }

        get("/student/fees/transactions/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val txns = transaction {
                val userRow = Users.select { Users.userId eq userId }.singleOrNull() ?: return@transaction emptyList<FeeTransactionApi>()
                StudentFeeTransactions.select { StudentFeeTransactions.studentId eq userRow[Users.id] }.map {
                    FeeTransactionApi(
                        id = it[StudentFeeTransactions.id],
                        amount = it[StudentFeeTransactions.amount].toDouble(),
                        description = it[StudentFeeTransactions.description],
                        date = it[StudentFeeTransactions.timestamp].toString(),
                        status = if (it[StudentFeeTransactions.isApproved]) "Approved" else "Pending",
                        academicYear = it[StudentFeeTransactions.academicYear],
                        semester = it[StudentFeeTransactions.semester]
                    )
                }
            }
            call.respond(txns)
        }

        get("/student/results/current/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val results = transaction {
                val userRow = Users.select { Users.userId eq userId }.singleOrNull() ?: return@transaction null
                val settings = SchoolSettings.selectAll().singleOrNull()
                val currentYear = settings?.get(SchoolSettings.currentAcademicYear) ?: "2024/2025"
                val currentSemester = settings?.get(SchoolSettings.currentSemester) ?: "First"

                val release = SemesterResultReleases.select { 
                    (SemesterResultReleases.academicYear eq currentYear) and (SemesterResultReleases.semester eq currentSemester) 
                }.singleOrNull()

                if (release == null || !release[SemesterResultReleases.isReleased]) return@transaction null

                val grades = (StudentCourseGrades innerJoin Courses).select {
                    (StudentCourseGrades.studentId eq userRow[Users.id]) and
                    (StudentCourseGrades.academicYear eq currentYear) and
                    (StudentCourseGrades.semester eq currentSemester)
                }.map {
                    TranscriptCourseApi(
                        code = it[Courses.code],
                        name = it[Courses.name],
                        credits = it[Courses.creditHours],
                        score = it[StudentCourseGrades.finalScore],
                        grade = it[StudentCourseGrades.gradeLetter]
                    )
                }

                SemesterResultApi(
                    academicYear = currentYear,
                    semester = currentSemester,
                    isReleased = true,
                    gpa = 4.0f, // Placeholder
                    totalCredits = grades.sumOf { it.credits },
                    results = grades
                )
            }
            if (results != null) call.respond(results) else call.respond(HttpStatusCode.NoContent)
        }

        get("/student/results/semester/{academicYear}/{semester}/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val academicYear = call.parameters["academicYear"] ?: ""
            val semester = call.parameters["semester"] ?: ""

            val results = transaction {
                val userRow = Users.select { Users.userId eq userId }.singleOrNull() ?: return@transaction null
                val release = SemesterResultReleases.select { 
                    (SemesterResultReleases.academicYear eq academicYear) and (SemesterResultReleases.semester eq semester) 
                }.singleOrNull()

                if (release == null || !release[SemesterResultReleases.isReleased]) return@transaction null

                val grades = (StudentCourseGrades innerJoin Courses).select {
                    (StudentCourseGrades.studentId eq userRow[Users.id]) and
                    (StudentCourseGrades.academicYear eq academicYear) and
                    (StudentCourseGrades.semester eq semester)
                }.map {
                    TranscriptCourseApi(
                        code = it[Courses.code],
                        name = it[Courses.name],
                        credits = it[Courses.creditHours],
                        score = it[StudentCourseGrades.finalScore],
                        grade = it[StudentCourseGrades.gradeLetter]
                    )
                }

                SemesterResultApi(
                    academicYear = academicYear,
                    semester = semester,
                    isReleased = true,
                    gpa = 4.0f, // Placeholder
                    totalCredits = grades.sumOf { it.credits },
                    results = grades
                )
            }
            if (results != null) call.respond(results) else call.respond(HttpStatusCode.NotFound, "Results not found or not released")
        }

        get("/student/results/summary/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val summary = transaction {
                val studentProfile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull() ?: return@transaction null
                AcademicSummaryApi(
                    cumulativeGpa = 4.0f, // Placeholder
                    totalCreditsEarned = 120, // Placeholder
                    academicStatus = studentProfile[StudentProfiles.academicStatus],
                    currentLevel = studentProfile[StudentProfiles.programmeLevel]
                )
            }
            if (summary != null) call.respond(summary) else call.respond(HttpStatusCode.NotFound)
        }

        get("/notifications/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val list = transaction {
                Notifications.select { Notifications.userId eq userId }.map {
                    NotificationApi(
                        id = it[Notifications.id],
                        title = it[Notifications.title],
                        message = it[Notifications.message],
                        date = it[Notifications.date],
                        isRead = it[Notifications.isRead]
                    )
                }
            }
            call.respond(list)
        }

        get("/calendar") {
            val events = transaction {
                AcademicCalendar.selectAll().map {
                    CalendarEventApi(
                        id = it[AcademicCalendar.id],
                        title = it[AcademicCalendar.label],
                        date = it[AcademicCalendar.date],
                        type = it[AcademicCalendar.breakType],
                        isWorkday = it[AcademicCalendar.isWorkday]
                    )
                }
            }
            call.respond(events)
        }

        get("/student/vclass/materials/{userId}") {
            val materials = transaction {
                // Simplified: return all materials
                Assignments.selectAll().map {
                    MaterialApi(
                        id = it[Assignments.id],
                        title = it[Assignments.title],
                        courseName = it[Assignments.courseName],
                        fileUrl = it[Assignments.filename] ?: "",
                        fileType = "PDF", // Default
                        uploadDate = it[Assignments.createdAt].toString()
                    )
                }
            }
            call.respond(materials)
        }

        get("/api/fees/balance/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val balance = transaction {
                val studentRow = StudentFeeBalances.select { StudentFeeBalances.studentId eq userId }.singleOrNull()
                if (studentRow != null) {
                    FeeBalanceApi(
                        balance = (studentRow[StudentFeeBalances.amountDue] - studentRow[StudentFeeBalances.amountPaid]).toDouble(),
                        paid = studentRow[StudentFeeBalances.amountPaid].toDouble(),
                        total = studentRow[StudentFeeBalances.amountDue].toDouble()
                    )
                } else FeeBalanceApi(0.0, 0.0, 0.0)
            }
            call.respond(balance)
        }

        post("/student/fees/pay") {
            val request = call.receive<PayFeesRequest>()
            val success = transaction {
                val userRow = Users.select { Users.userId eq request.userId }.singleOrNull() ?: return@transaction false
                StudentFeeTransactions.insert {
                    it[studentId] = userRow[Users.id]
                    it[amount] = request.amount.toFloat()
                    it[description] = request.description
                    it[academicYear] = request.academicYear
                    it[semester] = request.semester
                    it[timestamp] = LocalDateTime.now().toKotlinLocalDateTime()
                    it[isApproved] = false
                }.insertedCount > 0
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.InternalServerError)
        }

        get("/api/student/results/{userId}") {
            // Placeholder for now
            call.respond(emptyList<String>())
        }

        route("/student/transcript/api") {
            get("/semester/{academicYear}/{semester}") {
                val userId = call.parameters["userId"] ?: ""
                val academicYear = call.parameters["academicYear"] ?: ""
                val semester = call.parameters["semester"] ?: ""

                val transcript = transaction {
                    val userRow = Users.select { Users.userId eq userId }.singleOrNull() ?: return@transaction null
                    val release = SemesterResultReleases.select { 
                        (SemesterResultReleases.academicYear eq academicYear) and (SemesterResultReleases.semester eq semester) 
                    }.singleOrNull()

                    val grades = (StudentCourseGrades innerJoin Courses).select {
                        (StudentCourseGrades.studentId eq userRow[Users.id]) and
                        (StudentCourseGrades.academicYear eq academicYear) and
                        (StudentCourseGrades.semester eq semester)
                    }.map {
                        TranscriptCourseApi(
                            code = it[Courses.code],
                            name = it[Courses.name],
                            credits = it[Courses.creditHours],
                            score = it[StudentCourseGrades.finalScore],
                            grade = it[StudentCourseGrades.gradeLetter]
                        )
                    }

                    if (grades.isEmpty()) return@transaction null

                    val totalCredits = grades.sumOf { it.credits }
                    val gpa = if (totalCredits > 0) {
                        // Simplified GPA calculation
                        4.0f // Placeholder
                    } else 0.0f

                    TranscriptApi(
                        studentId = userId,
                        studentName = "${userRow[Users.firstName]} ${userRow[Users.lastName]}",
                        academicYear = academicYear,
                        semester = semester,
                        isReleased = release?.get(SemesterResultReleases.isReleased) ?: false,
                        gpa = gpa,
                        totalCredits = totalCredits,
                        courses = grades
                    )
                }

                if (transcript != null) call.respond(transcript) else call.respond(HttpStatusCode.NotFound, "Transcript not found")
            }

            get("/full/{userId}") {
                val userId = call.parameters["userId"] ?: ""

                val fullTranscript = transaction {
                    val userRow = Users.select { Users.userId eq userId }.singleOrNull() ?: return@transaction null
                    
                    val allGrades = (StudentCourseGrades innerJoin Courses).select {
                        StudentCourseGrades.studentId eq userRow[Users.id]
                    }.groupBy({ row -> row[StudentCourseGrades.academicYear] to row[StudentCourseGrades.semester] }) { row ->
                        TranscriptCourseApi(
                            code = row[Courses.code],
                            name = row[Courses.name],
                            credits = row[Courses.creditHours],
                            score = row[StudentCourseGrades.finalScore],
                            grade = row[StudentCourseGrades.gradeLetter]
                        )
                    }

                    val semesters = allGrades.map { (period, courses) ->
                        TranscriptSemesterApi(
                            academicYear = period.first,
                            semester = period.second,
                            gpa = 4.0f, // Placeholder
                            courses = courses
                        )
                    }

                    TranscriptApi(
                        studentId = userId,
                        studentName = "${userRow[Users.firstName]} ${userRow[Users.lastName]}",
                        gpa = 4.0f, // Cumulative GPA Placeholder
                        totalCredits = semesters.sumOf { it.courses.sumOf { c -> c.credits } },
                        semesters = semesters
                    )
                }
                if (fullTranscript != null) call.respond(fullTranscript) else call.respond(HttpStatusCode.NotFound, "Transcript not found")
            }
        }
    }
}
