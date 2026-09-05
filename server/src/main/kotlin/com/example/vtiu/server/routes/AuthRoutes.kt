package com.example.vtiu.server.routes

import com.example.vtiu.server.models.*
import com.example.vtiu.server.db.Users
import com.example.vtiu.server.db.Admins
import com.example.vtiu.server.db.StudentProfiles
import com.example.vtiu.server.db.TeacherProfiles
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.authRoutes() {
    route("/api") {
        post("/login") {
            call.respond(mapOf("message" to "Reached login route"))
        }

        get("/profile/{role}/{userId}") {
            val role = call.parameters["role"] ?: ""
            val userId = call.parameters["userId"] ?: ""

            val profile = transaction {
                val userRow = Users.select { Users.userId eq userId }.singleOrNull() ?: return@transaction null
                
                val profileData = when (role) {
                    "student" -> {
                        val studentRow = StudentProfiles.select { StudentProfiles.userId eq userId }.singleOrNull()
                        UserProfileData(
                            userId = userRow[Users.userId],
                            username = userRow[Users.username],
                            name = "${userRow[Users.firstName]} ${userRow[Users.lastName]}",
                            email = userRow[Users.email],
                            role = userRow[Users.role],
                            programme = studentRow?.get(StudentProfiles.currentProgramme),
                            level = studentRow?.get(StudentProfiles.programmeLevel),
                            indexNumber = studentRow?.get(StudentProfiles.indexNumber),
                            academicStatus = studentRow?.get(StudentProfiles.academicStatus),
                            profilePictureUrl = userRow[Users.profilePicture]
                        )
                    }
                    "teacher" -> {
                        val teacherRow = TeacherProfiles.select { TeacherProfiles.userId eq userId }.singleOrNull()
                        UserProfileData(
                            userId = userRow[Users.userId],
                            username = userRow[Users.username],
                            name = "${userRow[Users.firstName]} ${userRow[Users.lastName]}",
                            email = userRow[Users.email],
                            role = userRow[Users.role],
                            employeeId = teacherRow?.get(TeacherProfiles.employeeId),
                            department = teacherRow?.get(TeacherProfiles.department),
                            officeLocation = teacherRow?.get(TeacherProfiles.officeLocation),
                            profilePictureUrl = userRow[Users.profilePicture]
                        )
                    }
                    else -> null
                }
                profileData
            }

            if (profile != null) {
                call.respond(ProfileResponse(success = true, profile = profile))
            } else {
                call.respond(HttpStatusCode.NotFound, ProfileResponse(success = false, message = "Profile not found"))
            }
        }
    }
}
