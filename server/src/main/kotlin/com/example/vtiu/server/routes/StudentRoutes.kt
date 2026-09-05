package com.example.vtiu.server.routes

import com.example.vtiu.server.models.*
import com.example.vtiu.server.db.*
import io.ktor.http.*
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
