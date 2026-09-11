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
import java.time.format.DateTimeFormatter

fun Route.teacherRoutes() {
    route("/api/teacher") {
        get("/classes/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val classes = transaction {
                val teacherRow = TeacherProfiles.selectAll().where { TeacherProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<TeacherClassApi>()
                (TeacherCourseAssignments innerJoin Courses).selectAll().where { TeacherCourseAssignments.teacherId eq teacherRow[TeacherProfiles.id] }.map {
                    TeacherClassApi(
                        id = it[Courses.id],
                        courseName = it[Courses.name],
                        courseCode = it[Courses.code],
                        programme = it[Courses.programmeName],
                        level = it[Courses.programmeLevel]
                    )
                }
            }
            call.respond(classes)
        }

        get("/submissions/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val submissions = transaction {
                val teacherRow = TeacherProfiles.selectAll().where { TeacherProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<AssignmentSubmissionApi>()
                val teacherId = teacherRow[TeacherProfiles.id]
                
                (AssignmentSubmissions innerJoin Assignments innerJoin Users).selectAll().where { Assignments.courseId inList (
                    TeacherCourseAssignments.selectAll().where { TeacherCourseAssignments.teacherId eq teacherId }.map { it[TeacherCourseAssignments.courseId] }
                ) }.map {
                    val scoreVal = it[AssignmentSubmissions.score]
                    AssignmentSubmissionApi(
                        id = it[AssignmentSubmissions.id],
                        studentName = "${it[Users.firstName]} ${it[Users.lastName]}",
                        studentId = it[Users.userId],
                        assignmentTitle = it[Assignments.title],
                        submittedAt = it[AssignmentSubmissions.submittedAt].toString(),
                        filename = it[AssignmentSubmissions.filename],
                        score = scoreVal,
                        feedback = it[AssignmentSubmissions.feedback],
                        status = if (scoreVal != null) "Graded" else "Pending"
                    )
                }
            }
            call.respond(submissions)
        }

        post("/submissions/grade") {
            val request = call.receive<GradeSubmissionRequest>()
            val success = transaction {
                AssignmentSubmissions.update({ AssignmentSubmissions.id eq request.submissionId }) {
                    it[score] = request.score
                    it[feedback] = request.feedback
                    it[scoredAt] = LocalDateTime.now().toKotlinLocalDateTime()
                } > 0
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.BadRequest)
        }

        get("/schemes/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val schemes = transaction {
                val teacherRow = TeacherProfiles.selectAll().where { TeacherProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<CourseAssessmentSchemeApi>()
                (CourseAssessmentSchemes innerJoin Courses).selectAll().where { CourseAssessmentSchemes.teacherId eq teacherRow[TeacherProfiles.id] }.map {
                    CourseAssessmentSchemeApi(
                        id = it[CourseAssessmentSchemes.id],
                        courseId = it[Courses.id],
                        courseName = it[Courses.name],
                        quizWeight = it[CourseAssessmentSchemes.quizWeight],
                        assignmentWeight = it[CourseAssessmentSchemes.assignmentWeight],
                        examWeight = it[CourseAssessmentSchemes.examWeight]
                    )
                }
            }
            call.respond(schemes)
        }

        get("/performance/{courseId}") {
            // Simplified performance data
            call.respond(emptyList<ClassPerformanceApi>())
        }

        get("/timetable/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val entries = transaction {
                val teacherRow = TeacherProfiles.selectAll().where { TeacherProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<TimetableEntryApi>()
                (TimetableEntries innerJoin Courses).selectAll().where { TimetableEntries.courseId inList (
                    TeacherCourseAssignments.selectAll().where { TeacherCourseAssignments.teacherId eq teacherRow[TeacherProfiles.id] }.map { it[TeacherCourseAssignments.courseId] }
                ) }.map {
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

        get("/meetings/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            println("VClass: Teacher fetching meetings for $userId")
            val meetings = transaction {
                val userRow = Users.selectAll().where { Users.userId eq userId }.singleOrNull() ?: return@transaction emptyList<VClassMeetingApi>()
                val internalId = userRow[Users.id]
                
                // Use leftJoin in case some meetings aren't strictly tied to the course table records yet
                (Meetings leftJoin Courses).selectAll().where { Meetings.hostId eq internalId }.map {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val startStr = it[Meetings.scheduledStart]?.let { dt -> 
                        LocalDateTime.of(dt.year, dt.monthNumber, dt.dayOfMonth, dt.hour, dt.minute, dt.second).format(formatter)
                    } ?: ""
                    val endStr = it[Meetings.scheduledEnd]?.let { dt -> 
                        LocalDateTime.of(dt.year, dt.monthNumber, dt.dayOfMonth, dt.hour, dt.minute, dt.second).format(formatter)
                    } ?: ""

                    VClassMeetingApi(
                        id = it[Meetings.id],
                        title = it[Meetings.title],
                        courseName = it.getOrNull(Courses.name) ?: "General Session",
                        teacherName = "${userRow[Users.firstName]} ${userRow[Users.lastName]}",
                        start = startStr,
                        end = endStr,
                        isLive = true
                    )
                }
            }
            println("VClass: Found ${meetings.size} meetings for host $userId")
            call.respond(meetings)
        }
    }

    route("/api/attendance") {
        post("/mark") {
            val request = call.receive<MarkAttendanceRequest>()
            val success = transaction {
                val teacherRow = TeacherProfiles.selectAll().where { TeacherProfiles.userId eq request.teacherId }.singleOrNull() ?: return@transaction false
                val teacherIntId = teacherRow[TeacherProfiles.id]
                
                request.records.forEach { record ->
                    // Use update or insert pattern: delete existing for that day/student/course first to avoid duplicates
                    AttendanceRecords.deleteWhere { 
                        (AttendanceRecords.studentId eq record.studentId) and 
                        (AttendanceRecords.courseId eq request.courseId) and 
                        (AttendanceRecords.date eq request.date)
                    }
                    
                    AttendanceRecords.insert {
                        it[AttendanceRecords.studentId] = record.studentId
                        it[AttendanceRecords.teacherId] = teacherIntId
                        it[AttendanceRecords.courseId] = request.courseId
                        it[AttendanceRecords.date] = request.date
                        it[AttendanceRecords.isPresent] = record.isPresent
                    }
                }
                true
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.BadRequest)
        }

        get("/analytics/{courseId}") {
            val courseId = call.parameters["courseId"]?.toIntOrNull() ?: 0
            val analytics = transaction {
                // In a real app, calculate counts and percentages
                AttendanceRecords.selectAll().where { AttendanceRecords.courseId eq courseId }
                    .groupBy({ it[AttendanceRecords.studentId] }) { row ->
                        AttendanceAnalyticsApi(
                            studentName = row[AttendanceRecords.studentId], // Simplified
                            totalClasses = 1,
                            attendedCount = if (row[AttendanceRecords.isPresent]) 1 else 0,
                            attendancePercentage = if (row[AttendanceRecords.isPresent]) 100f else 0f
                        )
                    }.map { it.value.first() } // Very simplified
            }
            call.respond(analytics)
        }
    }
}
