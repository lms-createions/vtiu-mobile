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
        
        // Log available env keys for debugging (obscure values)
        val envKeys = System.getenv().keys
        println("DatabaseFactory: Available Env Keys: ${envKeys.filter { it.contains("DB") || it.contains("POSTGRES") || it.contains("URL") || it.contains("PG") }}")

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
        // Try various Railway environment variable patterns
        val databaseUrl = System.getenv("DATABASE_URL") ?: System.getenv("DATABASE_PUBLIC_URL")
        val pgUser = System.getenv("PGUSER") ?: System.getenv("POSTGRES_USER")
        val pgPass = System.getenv("PGPASSWORD") ?: System.getenv("POSTGRES_PASSWORD")
        val pgHost = System.getenv("PGHOST") ?: System.getenv("POSTGRES_HOST")
        val pgPort = System.getenv("PGPORT") ?: System.getenv("POSTGRES_PORT") ?: "5432"
        val pgDb = System.getenv("PGDATABASE") ?: System.getenv("POSTGRES_DB")

        // Priority 1: DATABASE_URL (Railway's primary way)
        if (!databaseUrl.isNullOrBlank()) {
            println("DatabaseFactory: Detected DATABASE_URL. Parsing...")
            return try {
                // Ensure it's compatible with URI (handle postgresql://)
                val sanitizedUrl = databaseUrl.replace("postgresql://", "postgres://")
                val uri = URI(sanitizedUrl)
                val userInfo = uri.userInfo ?: ":"
                val userPass = userInfo.split(":")
                val user = userPass.getOrElse(0) { "" }
                val password = userPass.getOrElse(1) { "" }
                val host = uri.host
                val port = if (uri.port != -1) uri.port else 5432
                val path = uri.path
                
                val jdbcUrl = "jdbc:postgresql://$host:$port$path?sslmode=require"
                println("DatabaseFactory: Connecting to $host:$port$path as user $user")
                createHikariDataSource(jdbcUrl, user, password)
            } catch (e: Exception) {
                println("DatabaseFactory: Failed to parse DATABASE_URL: ${e.message}. Falling back...")
                // Don't throw, let it try Priority 2
                internalGetDataSource(pgHost, pgPort, pgDb, pgUser, pgPass)
            }
        }

        return internalGetDataSource(pgHost, pgPort, pgDb, pgUser, pgPass)
    }

    private fun internalGetDataSource(pgHost: String?, pgPort: String?, pgDb: String?, pgUser: String?, pgPass: String?): HikariDataSource {
        // Priority 2: Individual variables
        if (!pgUser.isNullOrBlank() && !pgPass.isNullOrBlank() && !pgHost.isNullOrBlank()) {
            println("DatabaseFactory: Detected PG individual variables. Connecting to $pgHost...")
            val jdbcUrl = "jdbc:postgresql://$pgHost:$pgPort/$pgDb?sslmode=require"
            return createHikariDataSource(jdbcUrl, pgUser, pgPass)
        }

        // Fallback: Local development
        val localUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/vtiu"
        println("DatabaseFactory: No Railway variables found. Falling back to local/default: $localUrl")
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
            connectionTimeout = 30000 // 20 seconds
            initializationFailTimeout = 0 // Don't crash main thread if DB is slow
            validate()
        }
        return HikariDataSource(config)
    }
}
