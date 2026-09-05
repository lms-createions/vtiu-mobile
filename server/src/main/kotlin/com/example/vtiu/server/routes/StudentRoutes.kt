package com.example.vtiu.server.routes

import com.example.vtiu.server.models.*
import com.example.vtiu.server.db.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.studentRoutes() {
    route("/api") {
        get("/student/courses/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            
            val courses = transaction {
                // In a real app, join with student_course_registration
                // For now, return all courses for simplicity
                Courses.selectAll().map {
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
            // Placeholder for now
            call.respond(mapOf("balance" to 1200.0, "paid" to 800.0, "total" to 2000.0))
        }

        get("/api/student/results/{userId}") {
            // Placeholder for now
            call.respond(emptyList<String>())
        }
    }
}
