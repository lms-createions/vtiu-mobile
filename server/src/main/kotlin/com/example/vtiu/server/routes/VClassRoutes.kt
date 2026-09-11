package com.example.vtiu.server.routes

import com.example.vtiu.server.models.*
import com.example.vtiu.server.db.*
import com.example.vtiu.server.paystackClient
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

@Serializable
data class NetlessRoomRequest(val isRecord: Boolean = false, val limit: Int = 0)

@Serializable
data class NetlessRoomResponse(val uuid: String)

@Serializable
data class NetlessTokenRequest(val lifespan: Long = 3600000, val role: String = "admin")

fun Route.vClassRoutes() {
    route("/api") {
        // --- Virtual Class Materials ---
        get("/vclass/materials/{courseId}") {
            val courseId = call.parameters["courseId"]?.toIntOrNull() ?: 0
            val materials = transaction {
                // Find course name first since course_id column is missing in materials table
                val courseName = Courses.select { Courses.id eq courseId }.singleOrNull()?.get(Courses.name) ?: ""
                
                CourseMaterials.select { CourseMaterials.courseName eq courseName }.map {
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

        get("/vclass/material/{materialId}") {
            val materialId = call.parameters["materialId"]?.toIntOrNull() ?: 0
            val material = transaction {
                CourseMaterials.select { CourseMaterials.id eq materialId }.singleOrNull()?.let {
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
            if (material != null) call.respond(material) else call.respond(HttpStatusCode.NotFound)
        }

        // --- Assignments ---
        get("/vclass/assignments/{courseId}") {
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

        post("/assignments/submit") {
            val data = call.receive<Map<String, String>>()
            val studentId = data["student_id"] ?: ""
            val assignmentId = data["assignment_id"]?.toIntOrNull() ?: 0
            val filename = data["filename"] ?: ""

            val success = transaction {
                val userRow = Users.select { Users.userId eq studentId }.singleOrNull() ?: return@transaction false
                AssignmentSubmissions.insert {
                    it[AssignmentSubmissions.assignmentId] = assignmentId
                    it[AssignmentSubmissions.studentId] = userRow[Users.id]
                    it[AssignmentSubmissions.filename] = filename
                    it[AssignmentSubmissions.originalName] = filename
                    it[AssignmentSubmissions.submittedAt] = LocalDateTime.now().toKotlinLocalDateTime()
                }.insertedCount > 0
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.BadRequest)
        }

        // --- Meetings ---
        get("/vclass/meetings/{courseId}") {
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

        // --- Quizzes ---
        get("/student/quizzes/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val list = transaction {
                // Simplified: return all quizzes for the student's programme level
                val studentProfile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<QuizDetailApi>()
                val level = studentProfile[StudentProfiles.programmeLevel].toString()
                
                Quizzes.select { Quizzes.programmeLevel eq level }.map {
                    QuizDetailApi(
                        id = it[Quizzes.id],
                        title = it[Quizzes.title],
                        courseName = it[Quizzes.courseName],
                        durationMinutes = it[Quizzes.durationMinutes],
                        maxScore = 0f, 
                        startDatetime = it[Quizzes.startDatetime].toString(),
                        endDatetime = it[Quizzes.endDatetime].toString(),
                        attemptsAllowed = it[Quizzes.attemptsAllowed]
                    )
                }
            }
            call.respond(list)
        }

        get("/vclass/quiz/{quizId}") {
            val quizId = call.parameters["quizId"]?.toIntOrNull() ?: 0
            val quiz = transaction {
                val q = Quizzes.select { Quizzes.id eq quizId }.singleOrNull() ?: return@transaction null
                val questionsList = Questions.select { Questions.quizId eq quizId }.map { row ->
                    QuizQuestionApi(
                        id = row[Questions.id],
                        questionText = row[Questions.text],
                        questionType = row[Questions.questionType],
                        points = row[Questions.points],
                        options = Options.select { Options.questionId eq row[Questions.id] }.map { opt ->
                            QuizOptionApi(
                                id = opt[Options.id],
                                text = opt[Options.text],
                                isCorrect = false
                            )
                        }
                    )
                }
                QuizDetailApi(
                    id = q[Quizzes.id],
                    title = q[Quizzes.title],
                    courseName = q[Quizzes.courseName],
                    durationMinutes = q[Quizzes.durationMinutes],
                    maxScore = questionsList.sumOf { it.points.toDouble() }.toFloat(),
                    startDatetime = q[Quizzes.startDatetime].toString(),
                    endDatetime = q[Quizzes.endDatetime].toString(),
                    attemptsAllowed = q[Quizzes.attemptsAllowed],
                    questions = questionsList
                )
            }
            if (quiz != null) call.respond(quiz) else call.respond(HttpStatusCode.NotFound)
        }

        // --- Exams ---
        get("/student/exams/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val list = transaction {
                val studentProfile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<ExamSubmissionApi>()
                val level = studentProfile[StudentProfiles.programmeLevel].toString()
                
                Exams.select { Exams.programmeLevel eq level }.map {
                    ExamSubmissionApi(
                        id = it[Exams.id],
                        studentName = "N/A",
                        examTitle = it[Exams.title],
                        score = null,
                        submittedAt = ""
                    )
                }
            }
            call.respond(list)
        }

        get("/exam/{examId}") {
            val examId = call.parameters["examId"]?.toIntOrNull() ?: 0
            val exam = transaction {
                Exams.select { Exams.id eq examId }.singleOrNull()?.let {
                    mapOf(
                        "id" to it[Exams.id],
                        "title" to it[Exams.title],
                        "duration_minutes" to it[Exams.durationMinutes]
                    )
                }
            }
            if (exam != null) call.respond(exam) else call.respond(HttpStatusCode.NotFound)
        }

        // --- Whiteboard ---
        get("/vclass/whiteboard/{meetingId}") {
            val meetingId = call.parameters["meetingId"]?.toIntOrNull() ?: 0
            
            // 1. Get Whiteboard Settings and check if roomUuid already exists
            val (appId, sdkToken, existingUuid) = transaction {
                val settings = SchoolSettings.selectAll().singleOrNull() ?: return@transaction Triple("", "", null)
                val meeting = Meetings.select { Meetings.id eq meetingId }.singleOrNull()
                Triple(
                    settings[SchoolSettings.agoraWhiteboardId],
                    settings[SchoolSettings.agoraWhiteboardToken],
                    meeting?.get(Meetings.whiteboardRoomUuid)
                )
            }

            if (appId.isBlank()) {
                return@get call.respond(HttpStatusCode.PreconditionFailed, "Whiteboard App ID missing")
            }

            try {
                var roomUuid = existingUuid

                // Step A: Create a Room only if it doesn't exist
                if (roomUuid == null) {
                    val createResponse = paystackClient.post("https://api.netless.link/v1/rooms") {
                        header("token", sdkToken)
                        contentType(ContentType.Application.Json)
                        setBody(NetlessRoomRequest())
                    }
                    
                    if (createResponse.status != HttpStatusCode.Created && createResponse.status != HttpStatusCode.OK) {
                        val error = createResponse.body<String>()
                        return@get call.respond(HttpStatusCode.InternalServerError, "Netless Room Creation Failed: $error")
                    }
                    
                    roomUuid = createResponse.body<NetlessRoomResponse>().uuid
                    
                    // Save the new UUID to the database
                    transaction {
                        Meetings.update({ Meetings.id eq meetingId }) {
                            it[whiteboardRoomUuid] = roomUuid
                        }
                    }
                }
                
                // Step B: Generate a Room Token for the (new or existing) UUID
                val tokenResponse = paystackClient.post("https://api.netless.link/v1/tokens/rooms/$roomUuid") {
                    header("token", sdkToken)
                    contentType(ContentType.Application.Json)
                    setBody(NetlessTokenRequest())
                }
                
                val roomToken = tokenResponse.body<String>().replace("\"", "") // Simple string return
                
                call.respond(WhiteboardRoomResponse(
                    appId = appId,
                    roomUuid = roomUuid,
                    roomToken = roomToken
                ))
                
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Whiteboard Error: ${e.message}")
            }
        }

        // --- Student Dashboard Views ---
        get("/student/vclass/meetings/{userId}") {
            val userId = call.parameters["userId"] ?: ""
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

        get("/student/vclass/materials/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val materials = transaction {
                val studentProfile = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<MaterialApi>()
                val programme = studentProfile[StudentProfiles.currentProgramme]
                val level = studentProfile[StudentProfiles.programmeLevel].toString()
                
                CourseMaterials.select { (CourseMaterials.programmeName eq programme) and (CourseMaterials.programmeLevel eq level) }.map {
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
    }
}
