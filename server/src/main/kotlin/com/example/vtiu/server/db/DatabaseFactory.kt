package com.example.vtiu.server.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

object DatabaseFactory {
    fun init() {
        println("--- DATABASE INITIALIZATION START ---")
        
        val dataSource = try {
            getDataSource()
        } catch (e: Exception) {
            println("DatabaseFactory: FATAL - Could not create DataSource: ${e.message}")
            e.printStackTrace()
            return
        }

        println("DatabaseFactory: Attempting to connect via Exposed...")
        Database.connect(dataSource)
        
        try {
            transaction {
                println("DatabaseFactory: Starting SchemaUtils.create (Creating tables if missing)...")
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
                println("DatabaseFactory: SchemaUtils.create finished successfully.")
            }
            println("--- DATABASE INITIALIZATION COMPLETE ---")
        } catch (e: Exception) {
            println("DatabaseFactory: ERROR - Table creation failed: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun getDataSource(): HikariDataSource {
        val databaseUrl = System.getenv("DATABASE_URL")
        val pgUser = System.getenv("PGUSER")
        val pgPass = System.getenv("PGPASSWORD")
        val pgHost = System.getenv("PGHOST")
        val pgPort = System.getenv("PGPORT")
        val pgDb = System.getenv("PGDATABASE")

        // Priority 1: DATABASE_URL (Railway's primary way)
        if (!databaseUrl.isNullOrBlank()) {
            println("DatabaseFactory: Detected DATABASE_URL. Parsing...")
            return try {
                val uri = URI(databaseUrl)
                val userInfo = uri.userInfo ?: ":"
                val userPass = userInfo.split(":")
                val user = userPass.getOrElse(0) { "" }
                val password = userPass.getOrElse(1) { "" }
                val host = uri.host
                val port = if (uri.port != -1) uri.port else 5432
                val path = uri.path // Includes leading /
                
                val jdbcUrl = "jdbc:postgresql://$host:$port$path?sslmode=require"
                println("DatabaseFactory: Connecting to $host:$port$path as user $user")
                createHikariDataSource(jdbcUrl, user, password)
            } catch (e: Exception) {
                println("DatabaseFactory: Failed to parse DATABASE_URL: ${e.message}. Falling back...")
                throw e
            }
        }

        // Priority 2: Individual variables
        if (!pgUser.isNullOrBlank() && !pgPass.isNullOrBlank() && !pgHost.isNullOrBlank()) {
            println("DatabaseFactory: Detected PG individual variables. Connecting to $pgHost...")
            val jdbcUrl = "jdbc:postgresql://$pgHost:$pgPort/$pgDb?sslmode=require"
            return createHikariDataSource(jdbcUrl, pgUser!!, pgPass!!)
        }

        // Fallback: Local development
        val localUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/vtiu"
        println("DatabaseFactory: No Railway variables found. Falling back to local: $localUrl")
        return createHikariDataSource(
            localUrl, 
            System.getenv("DB_USER") ?: "postgres", 
            System.getenv("DB_PASSWORD") ?: "password"
        )
    }

    private fun createHikariDataSource(url: String, user: String, pass: String): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = url
            username = user
            password = pass
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            connectionTimeout = 20000 // 20 seconds
            initializationFailTimeout = 0 // Don't crash main thread if DB is slow
            validate()
        }
        return HikariDataSource(config)
    }
}
