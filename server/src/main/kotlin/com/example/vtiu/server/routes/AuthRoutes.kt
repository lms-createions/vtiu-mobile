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
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

fun Route.authRoutes() {
    route("/api") {
        get("/debug/user/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val user = transaction {
                Users.select { Users.userId eq userId }.map {
                    mapOf(
                        "userId" to it[Users.userId],
                        "username" to it[Users.username],
                        "role" to it[Users.role],
                        "passwordHash" to it[Users.passwordHash]
                    )
                }.singleOrNull()
            }
            if (user != null) call.respond(user) else call.respond(HttpStatusCode.NotFound)
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                
                val user = transaction {
                    println("Login attempt: ID=${request.userId}, Username=${request.username}, Role=${request.role}")
                    // Find user by userId and role first
                    // Case-insensitive ID and Role check
                    val query = Users.select { 
                        (Users.userId.lowerCase() eq request.userId.trim().lowercase()) and 
                        (Users.role.lowerCase() eq request.role.trim().lowercase()) 
                    }
                    
                    val userRow = query.singleOrNull()
                    
                    if (userRow == null) {
                        println("Login failed: User not found with ID ${request.userId} and Role ${request.role}")
                        return@transaction null
                    }

                    val dbUsername = userRow[Users.username].trim()
                    val dbUserId = userRow[Users.userId].trim()
                    val reqUsername = request.username.trim()
                    val dbPassHash = userRow[Users.passwordHash]

                    println("Comparing: DB_User='$dbUsername', DB_ID='$dbUserId' vs REQ_User='$reqUsername'")

                    // Verify Username (Email) OR User ID matches the provided username field
                    val usernameMatches = dbUsername.equals(reqUsername, ignoreCase = true) || 
                                          dbUserId.equals(reqUsername, ignoreCase = true)

                    // Verify password (supports plain text and Werkzeug-style pbkdf2 hashes)
                    if (usernameMatches && checkPassword(request.password, dbPassHash)) {
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
                    } else {
                        println("Login failed: Credential mismatch for $dbUserId")
                        null
                    }
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
                val userRow = Users.select { Users.userId.lowerCase() eq userId.trim().lowercase() }.singleOrNull() ?: return@transaction null
                
                val rawProfilePic = userRow[Users.profilePicture]
                val profilePicPath = if (rawProfilePic.isNullOrBlank() || rawProfilePic == "default.png") {
                    "/static/uploads/profile_pictures/default_avatar.png"
                } else {
                    if (rawProfilePic.startsWith("/static")) rawProfilePic 
                    else "/static/uploads/profile_pictures/${rawProfilePic.substringAfterLast("/")}"
                }

                val profileData = when (role.lowercase()) {
                    "student" -> {
                        val studentRow = StudentProfiles.select { StudentProfiles.userId.lowerCase() eq userRow[Users.userId].lowercase() }.singleOrNull()
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
                            profilePictureUrl = profilePicPath,
                            academicYear = studentRow?.get(StudentProfiles.academicYear),
                            semester = studentRow?.get(StudentProfiles.semester)
                        )
                    }
                    "teacher" -> {
                        val teacherRow = TeacherProfiles.select { TeacherProfiles.userId.lowerCase() eq userRow[Users.userId].lowercase() }.singleOrNull()
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

private fun checkPassword(password: String, hashed: String): Boolean {
    if (password == hashed) return true // Plain text match
    
    return try {
        if (hashed.startsWith("pbkdf2:sha256:")) {
            val parts = hashed.split("$")
            if (parts.size != 3) return false
            
            val header = parts[0].split(":")
            val iterations = header.getOrNull(2)?.toIntOrNull() ?: 260000
            val salt = parts[1]
            val hash = parts[2]
            
            val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), iterations, 256)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val actualHash = factory.generateSecret(spec).encoded
            val actualHashHex = actualHash.joinToString("") { "%02x".format(it) }
            
            actualHashHex == hash
        } else {
            // If it's not a PBKDF2 hash, just return false since plain text check already failed
            false
        }
    } catch (e: Exception) {
        println("Password verification error: ${e.message}")
        false
    }
}
