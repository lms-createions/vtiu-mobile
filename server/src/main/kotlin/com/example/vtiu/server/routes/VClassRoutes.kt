package com.example.vtiu.server.routes

import com.example.vtiu.server.models.*
import com.example.vtiu.server.db.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.vClassRoutes() {
    route("/api/vclass") {
        get("/materials/{courseId}") {
            val courseId = call.parameters["courseId"]?.toIntOrNull() ?: 0
            val materials = transaction {
                CourseMaterials.select { CourseMaterials.id eq courseId }.map { // CourseMaterials is better for this
                    MaterialApi(
                        id = it[CourseMaterials.id],
                        title = it[CourseMaterials.title],
                        courseName = it[CourseMaterials.courseName],
                        fileUrl = it[CourseMaterials.filename],
                        fileType = it[CourseMaterials.fileType],
                        uploadDate = it[CourseMaterials.uploadDate].toString()
                    )
                }
            }
            call.respond(materials)
        }

        get("/assignments/{courseId}") {
            val courseId = call.parameters["courseId"]?.toIntOrNull() ?: 0
            val list = transaction {
                Assignments.select { Assignments.courseId eq courseId }.map {
                    VClassAssignmentApi(
                        id = it[Assignments.id],
                        title = it[Assignments.title],
                        courseName = it[Assignments.courseName],
                        dueDate = it[Assignments.dueDate].toString(),
                        maxScore = it[Assignments.maxScore]
                    )
                }
            }
            call.respond(list)
        }

        get("/meetings/{courseId}") {
            val courseId = call.parameters["courseId"]?.toIntOrNull() ?: 0
            val meetings = transaction {
                (Meetings innerJoin Courses).select { Meetings.courseId eq courseId }.map {
                    VClassMeetingApi(
                        id = it[Meetings.id],
                        title = it[Meetings.title],
                        description = it[Meetings.description],
                        courseName = it[Courses.name],
                        scheduledStart = it[Meetings.scheduledStart].toString(),
                        scheduledEnd = it[Meetings.scheduledEnd].toString(),
                        joinUrl = it[Meetings.joinUrl],
                        meetingCode = it[Meetings.meetingCode]
                    )
                }
            }
            call.respond(meetings)
        }

        get("/quiz/{quizId}") {
            val quizId = call.parameters["quizId"]?.toIntOrNull() ?: 0
            val quiz = transaction {
                val q = Quizzes.select { Quizzes.id eq quizId }.singleOrNull() ?: return@transaction null
                val questions = Questions.select { Questions.quizId eq quizId }.map { row ->
                    QuizQuestionApi(
                        id = row[Questions.id],
                        questionText = row[Questions.text],
                        questionType = row[Questions.questionType],
                        points = row[Questions.points],
                        options = Options.select { Options.questionId eq row[Questions.id] }.map { opt ->
                            QuizOptionApi(
                                id = opt[Options.id],
                                text = opt[Options.text],
                                isCorrect = false // Don't send the answer to the student
                            )
                        }
                    )
                }
                QuizDetailApi(
                    id = q[Quizzes.id],
                    title = q[Quizzes.title],
                    courseName = q[Quizzes.courseName],
                    durationMinutes = q[Quizzes.durationMinutes],
                    maxScore = 0f, // Needs sum of points
                    startDatetime = q[Quizzes.startDatetime].toString(),
                    endDatetime = q[Quizzes.endDatetime].toString(),
                    attemptsAllowed = q[Quizzes.attemptsAllowed],
                    questions = questions
                )
            }
            if (quiz != null) call.respond(quiz) else call.respond(HttpStatusCode.NotFound)
        }
    }

    route("/api/student") {
        get("/vclass/meetings/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            // Simplified: return all meetings for now. 
            // In a real app, join with student_course_registration
            val meetings = transaction {
                (Meetings innerJoin Courses).selectAll().map {
                    VClassMeetingApi(
                        id = it[Meetings.id],
                        title = it[Meetings.title],
                        description = it[Meetings.description],
                        courseName = it[Courses.name],
                        scheduledStart = it[Meetings.scheduledStart].toString(),
                        scheduledEnd = it[Meetings.scheduledEnd].toString(),
                        joinUrl = it[Meetings.joinUrl],
                        meetingCode = it[Meetings.meetingCode]
                    )
                }
            }
            call.respond(meetings)
        }
    }
}
