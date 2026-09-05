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
                val teacherProfile = TeacherProfiles.select { TeacherProfiles.userId eq userId }.singleOrNull()
                    ?: return@transaction emptyList<TeacherClassApi>()
                
                (TeacherCourseAssignments innerJoin Courses).select { TeacherCourseAssignments.teacherId eq teacherProfile[TeacherProfiles.id] }.map {
                    TeacherClassApi(
                        id = it[Courses.id],
                        courseName = it[Courses.name],
                        courseCode = it[Courses.code],
                        programme = it[Courses.programmeName],
                        level = it[Courses.programmeLevel],
                        studentCount = StudentCourseRegistrations.select { StudentCourseRegistrations.courseId eq it[Courses.id] }.count().toInt()
                    )
                }
            }
            call.respond(classes)
        }

        get("/submissions/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val submissions = transaction {
                val teacherProfile = TeacherProfiles.select { TeacherProfiles.userId eq userId }.singleOrNull()
                    ?: return@transaction emptyList<AssignmentSubmissionApi>()
                
                val assignedCourseIds = TeacherCourseAssignments.select { TeacherCourseAssignments.teacherId eq teacherProfile[TeacherProfiles.id] }.map { it[TeacherCourseAssignments.courseId] }
                
                (AssignmentSubmissions innerJoin Assignments innerJoin Users).select { Assignments.courseId inList assignedCourseIds }.map {
                    AssignmentSubmissionApi(
                        id = it[AssignmentSubmissions.id],
                        studentName = "${it[Users.firstName]} ${it[Users.lastName]}",
                        studentId = it[Users.userId],
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
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.InternalServerError)
        }

        get("/schemes/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val schemes = transaction {
                val teacherProfile = TeacherProfiles.select { TeacherProfiles.userId eq userId }.singleOrNull()
                    ?: return@transaction emptyList<CourseAssessmentSchemeApi>()
                
                (CourseAssessmentSchemes innerJoin Courses).select { CourseAssessmentSchemes.teacherId eq teacherProfile[TeacherProfiles.id] }.map {
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

        post("/update_scheme") {
            val scheme = call.receive<CourseAssessmentSchemeApi>()
            val success = transaction {
                CourseAssessmentSchemes.update({ CourseAssessmentSchemes.id eq scheme.id }) {
                    it[quizWeight] = scheme.quizWeight
                    it[assignmentWeight] = scheme.assignmentWeight
                    it[examWeight] = scheme.examWeight
                } > 0
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.InternalServerError)
        }

        get("/slots/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val slots = transaction {
                val teacherProfile = TeacherProfiles.select { TeacherProfiles.userId eq userId }.singleOrNull()
                    ?: return@transaction emptyList<TeacherSlotApi>()
                
                AppointmentSlots.select { AppointmentSlots.teacherId eq teacherProfile[TeacherProfiles.id] }.map {
                    TeacherSlotApi(
                        id = it[AppointmentSlots.id],
                        date = it[AppointmentSlots.date],
                        startTime = it[AppointmentSlots.startTime],
                        endTime = it[AppointmentSlots.endTime],
                        isBooked = it[AppointmentSlots.isBooked]
                    )
                }
            }
            call.respond(slots)
        }

        post("/slots/create") {
            val data = call.receive<Map<String, String>>()
            val userId = data["user_id"] ?: ""
            val success = transaction {
                val teacherProfile = TeacherProfiles.select { TeacherProfiles.userId eq userId }.singleOrNull() ?: return@transaction false
                AppointmentSlots.insert {
                    it[teacherId] = teacherProfile[TeacherProfiles.id]
                    it[date] = data["date"] ?: ""
                    it[startTime] = data["start"] ?: ""
                    it[endTime] = data["end"] ?: ""
                    it[isBooked] = false
                }.insertedCount > 0
            }
            if (success) call.respond(HttpStatusCode.Created) else call.respond(HttpStatusCode.BadRequest)
        }
    }

    route("/api/attendance") {
        post("/mark") {
            val request = call.receive<MarkAttendanceRequest>()
            val success = transaction {
                // Determine teacher ID from course or session in real app
                // Placeholder: assuming teacher ID 1 for now
                AttendanceRecords.insert {
                    it[studentId] = request.studentId
                    it[teacherId] = 1 
                    it[courseId] = request.courseId
                    it[date] = request.date
                    it[isPresent] = request.isPresent
                }.insertedCount > 0
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
