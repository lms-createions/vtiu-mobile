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
            try {
                val request = call.receive<LoginRequest>()
                
                val user = transaction {
                    println("Login attempt: ID=${request.userId}, Username=${request.username}, Role=${request.role}")
                    // Find user by userId and role first
                    val query = Users.select { 
                        (Users.userId eq request.userId.trim()) and (Users.role eq request.role) 
                    }
                    
                    val userRow = query.singleOrNull()
                    
                    if (userRow == null) {
                        println("Login failed: User not found with ID ${request.userId} and Role ${request.role}")
                        return@transaction null
                    }

                    val dbUsername = userRow[Users.username].trim()
                    val dbUserId = userRow[Users.userId].trim()
                    val reqUsername = request.username.trim()
                    val dbPass = userRow[Users.passwordHash]

                    println("Comparing: DB_User='$dbUsername', DB_ID='$dbUserId' vs REQ_User='$reqUsername'")

                    // Verify Username (Email) OR User ID matches the provided username field
                    // AND verify the password matches
                    val usernameMatches = dbUsername.equals(reqUsername, ignoreCase = true) || 
                                          dbUserId.equals(reqUsername, ignoreCase = true)

                    if (usernameMatches && dbPass == request.password) {
                        println("Login successful for $dbUserId")
                        val rawProfilePic = userRow[Users.profilePicture]
                        val profilePicPath = if (rawProfilePic.isNullOrBlank() || rawProfilePic == "default.png") {
                            "/static/uploads/profile_pictures/default_avatar.png"
                        } else {
                            if (rawProfilePic.startsWith("/static")) rawProfilePic 
                            else "/static/uploads/profile_pictures/${rawProfilePic.substringAfterLast("/")}"
                        }
                        
                        UserData(
                            id = userRow[Users.id],
                            userId = userRow[Users.userId],
                            name = "${userRow[Users.firstName]} ${userRow[Users.lastName]}",
                            role = userRow[Users.role],
                            profilePictureUrl = profilePicPath
                        )
                    } else null
                }

                if (user != null) {
                    call.respond(LoginResponse(success = true, user = user))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, LoginResponse(success = false, message = "Invalid credentials. Please verify your Username, ID, and Password."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, LoginResponse(success = false, message = "Bad Request: ${e.message}"))
            }
        }

        get("/profile/{role}/{userId}") {
            val role = call.parameters["role"] ?: ""
            val userId = call.parameters["userId"] ?: ""

            val profile = transaction {
                val userRow = Users.select { Users.userId eq userId }.singleOrNull() ?: return@transaction null
                
                val rawProfilePic = userRow[Users.profilePicture]
                val profilePicPath = if (rawProfilePic.isNullOrBlank() || rawProfilePic == "default.png") {
                    "/static/uploads/profile_pictures/default_avatar.png"
                } else {
                    if (rawProfilePic.startsWith("/static")) rawProfilePic 
                    else "/static/uploads/profile_pictures/${rawProfilePic.substringAfterLast("/")}"
                }

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
                            profilePictureUrl = profilePicPath
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
                            profilePictureUrl = profilePicPath
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
