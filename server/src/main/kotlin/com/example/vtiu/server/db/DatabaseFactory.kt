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
        val dataSource = if (databaseUrl != null) {
            // Railway provides DATABASE_URL in format: postgres://user:password@host:port/database
            val uri = URI(databaseUrl)
            val userInfo = uri.userInfo.split(":")
            val user = userInfo[0]
            val password = userInfo[1]
            val url = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}?sslmode=require"
            
            HikariDataSource(HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = url
                username = user
                this.password = password
                maximumPoolSize = 3
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            })
        } else {
            // Local development fallback (h2 or local postgres)
            HikariDataSource(HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = "jdbc:postgresql://localhost:5432/vtiu"
                username = "postgres"
                password = "password"
                maximumPoolSize = 3
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            })
        }
        
        Database.connect(dataSource)
        
        transaction {
            SchemaUtils.create(
                Admins, Users, StudentProfiles, TeacherProfiles,
                Courses, Assignments, Quizzes, Exams,
                StudentFeeTransactions, StudentFeeBalances,
                Notifications, AppointmentBookings, AppointmentSlots,
                AcademicCalendar, TimetableEntries, StudentCourseGrades,
                Meetings, Questions, Options, StudentQuizSubmissions, CourseMaterials
            )
        }
    }
}
