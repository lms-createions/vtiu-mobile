package com.example.vtiu.server.routes

import com.example.vtiu.server.models.*
import com.example.vtiu.server.db.*
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
                Assignments.select { Assignments.courseId eq courseId }.map {
                    MaterialApi(
                        id = it[Assignments.id],
                        title = it[Assignments.title],
                        courseName = it[Assignments.courseName],
                        fileUrl = it[Assignments.filename] ?: "",
                        fileType = "PDF",
                        uploadDate = it[Assignments.createdAt].toString()
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
    }
}
