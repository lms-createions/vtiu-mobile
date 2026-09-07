package com.example.vtiu.server.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

object DatabaseFactory {
    fun init() {
        val databaseUrl = System.getenv("DATABASE_URL")
        val pgUser = System.getenv("PGUSER")
        val pgPass = System.getenv("PGPASSWORD")
        val pgHost = System.getenv("PGHOST")
        val pgPort = System.getenv("PGPORT")
        val pgDb = System.getenv("PGDATABASE")

        val dataSource = if (!databaseUrl.isNullOrBlank()) {
            println("Initializing Database with DATABASE_URL...")
            val uri = URI(databaseUrl)
            val userInfo = uri.userInfo ?: ":"
            val userPass = userInfo.split(":")
            val user = userPass.getOrElse(0) { "" }
            val password = userPass.getOrElse(1) { "" }
            val host = uri.host
            val port = if (uri.port != -1) uri.port else 5432
            val path = uri.path
            
            val url = "jdbc:postgresql://$host:$port$path?sslmode=require"
            createHikariDataSource(url, user, password)
        } else if (!pgUser.isNullOrBlank() && !pgPass.isNullOrBlank()) {
            println("Initializing Database with individual PG variables...")
            val url = "jdbc:postgresql://$pgHost:$pgPort/$pgDb?sslmode=require"
            createHikariDataSource(url, pgUser, pgPass)
        } else {
            // Local development fallback
            val localUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/vtiu"
            println("Initializing Database with Local fallback: $localUrl")
            createHikariDataSource(
                localUrl, 
                System.getenv("DB_USER") ?: "postgres", 
                System.getenv("DB_PASSWORD") ?: "password"
            )
        }
        
        Database.connect(dataSource)
        
        transaction {
            SchemaUtils.create(
                Admins, Users, StudentProfiles, TeacherProfiles,
                Courses, Assignments, Quizzes, Exams,
                StudentFeeTransactions, StudentFeeBalances,
                Notifications, AppointmentBookings, AppointmentSlots,
                AcademicCalendar, TimetableEntries, StudentCourseGrades,
                Meetings, Questions, Options, StudentQuizSubmissions, CourseMaterials,
                TeacherCourseAssignments, StudentCourseRegistrations, AttendanceRecords,
                CourseAssessmentSchemes, AssignmentSubmissions, SemesterResultReleases,
                SchoolSettings, ProgrammeFeeStructures
            )
        }
    }

    private fun createHikariDataSource(url: String, user: String, pass: String) = 
        HikariDataSource(HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = url
            username = user
            password = pass
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        })
}
