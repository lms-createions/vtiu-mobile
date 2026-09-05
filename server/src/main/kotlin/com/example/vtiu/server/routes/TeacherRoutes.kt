package com.example.vtiu.server.routes

import com.example.vtiu.server.models.*
import com.example.vtiu.server.db.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.teacherRoutes() {
    route("/api") {
        get("/teacher/classes/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            
            val classes = transaction {
                // Simplified: return all courses
                Courses.selectAll().map {
                    TeacherClassApi(
                        id = it[Courses.id],
                        courseName = it[Courses.name],
                        courseCode = it[Courses.code],
                        programme = it[Courses.programmeName],
                        level = it[Courses.programmeLevel],
                        studentCount = 0 // Needs a join/count
                    )
                }
            }
            call.respond(classes)
        }
    }
}
