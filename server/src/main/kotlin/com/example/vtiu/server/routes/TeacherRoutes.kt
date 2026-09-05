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
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun Route.teacherRoutes() {
    route("/api/teacher") {
        get("/classes/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val classes = transaction {
                val teacherRow = TeacherProfiles.select { TeacherProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<TeacherClassApi>()
                (TeacherCourseAssignments innerJoin Courses).select { TeacherCourseAssignments.teacherId eq teacherRow[TeacherProfiles.id] }.map {
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
                val teacherRow = TeacherProfiles.select { TeacherProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<AssignmentSubmissionApi>()
                val teacherId = teacherRow[TeacherProfiles.id]
                
                (AssignmentSubmissions innerJoin Assignments).select { Assignments.courseId inList (
                    TeacherCourseAssignments.select { TeacherCourseAssignments.teacherId eq teacherId }.map { it[TeacherCourseAssignments.courseId] }
                ) }.map {
                    AssignmentSubmissionApi(
                        id = it[AssignmentSubmissions.id],
                        studentName = "Student Name", // Simplified
                        studentId = "ID",
                        assignmentTitle = it[Assignments.title],
                        submittedAt = it[AssignmentSubmissions.submittedAt].toString(),
                        filename = it[AssignmentSubmissions.filename],
                        score = it[AssignmentSubmissions.score],
                        feedback = it[AssignmentSubmissions.feedback]
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
                val teacherRow = TeacherProfiles.select { TeacherProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<CourseAssessmentSchemeApi>()
                (CourseAssessmentSchemes innerJoin Courses).select { CourseAssessmentSchemes.teacherId eq teacherRow[TeacherProfiles.id] }.map {
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
                val teacherRow = TeacherProfiles.select { TeacherProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<TimetableEntryApi>()
                (TimetableEntries innerJoin Courses).select { TimetableEntries.courseId inList (
                    TeacherCourseAssignments.select { TeacherCourseAssignments.teacherId eq teacherRow[TeacherProfiles.id] }.map { it[TeacherCourseAssignments.courseId] }
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
    }

    route("/api/attendance") {
        post("/mark") {
            val request = call.receive<MarkAttendanceRequest>()
            val success = transaction {
                AttendanceRecords.insert {
                    it[studentId] = request.studentId
                    it[courseId] = request.courseId
                    it[date] = request.date
                    it[isPresent] = request.isPresent
                    // Missing teacherId in request, would need it in a real app
                }.insertedCount > 0
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.BadRequest)
        }

        get("/analytics/{courseId}") {
            val courseId = call.parameters["courseId"]?.toIntOrNull() ?: 0
            val analytics = transaction {
                // In a real app, calculate counts and percentages
                AttendanceRecords.select { AttendanceRecords.courseId eq courseId }
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
