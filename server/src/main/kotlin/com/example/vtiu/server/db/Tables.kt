package com.example.vtiu.server.db

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val userId = varchar("user_id", 50)
    val username = varchar("username", 50)
    val password = varchar("password", 255) // Added for login
    val firstName = varchar("first_name", 100)
    val middleName = varchar("middle_name", 100).nullable()
    val lastName = varchar("last_name", 100)
    val role = varchar("role", 20)
    val profilePicture = varchar("profile_picture", 255).nullable()

    override val primaryKey = PrimaryKey(userId)
}

object Courses : Table("courses") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val code = varchar("code", 50)
    val programmeName = varchar("programme_name", 255)
    val programmeLevel = varchar("programme_level", 50)
    val semester = varchar("semester", 20)
    val creditHours = integer("credit_hours")
    val isMandatory = bool("is_mandatory").default(true)

    override val primaryKey = PrimaryKey(id)
}

object Fees : Table("fees") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 50)
    val amount = decimal("amount", 10, 2)
    val description = varchar("description", 255)
    val date = varchar("date", 50) // Simplified as String for now
    val status = varchar("status", 20)

    override val primaryKey = PrimaryKey(id)
}

object Notifications : Table("notifications") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 50)
    val title = varchar("title", 255)
    val message = text("message")
    val date = varchar("date", 50)
    val isRead = bool("is_read").default(false)

    override val primaryKey = PrimaryKey(id)
}

object Appointments : Table("appointments") {
    val id = integer("id").autoIncrement()
    val userId = varchar("user_id", 50)
    val teacherId = varchar("teacher_id", 50)
    val title = varchar("title", 255)
    val date = varchar("date", 50)
    val startTime = varchar("start_time", 20)
    val endTime = varchar("end_time", 20)
    val status = varchar("status", 20)

    override val primaryKey = PrimaryKey(id)
}
