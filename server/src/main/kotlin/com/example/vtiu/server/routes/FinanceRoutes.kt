package com.example.vtiu.server.routes

import com.example.vtiu.server.models.*
import com.example.vtiu.server.db.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun Route.financeRoutes() {
    route("/api/finance") {
        get("/summary") {
            val summary = transaction {
                val totalRevenue = StudentFeeTransactions.select { StudentFeeTransactions.isApproved eq true }
                    .sumOf { it[StudentFeeTransactions.amount].toDouble() }
                
                val outstanding = StudentFeeBalances.selectAll()
                    .sumOf { (it[StudentFeeBalances.amountDue] - it[StudentFeeBalances.amountPaid]).toDouble() }
                
                val pendingCount = StudentFeeTransactions.select { StudentFeeTransactions.isApproved eq false }.count()
                
                mapOf(
                    "total_revenue" to totalRevenue,
                    "outstanding_balance" to outstanding,
                    "pending_count" to pendingCount,
                    "collection_rate" to if (totalRevenue + outstanding > 0) (totalRevenue / (totalRevenue + outstanding) * 100) else 0.0
                )
            }
            call.respond(summary)
        }

        get("/payments") {
            val payments = transaction {
                (StudentFeeTransactions innerJoin Users).selectAll().map {
                    mapOf(
                        "id" to it[StudentFeeTransactions.id],
                        "student_name" to "${it[Users.firstName]} ${it[Users.lastName]}",
                        "amount" to it[StudentFeeTransactions.amount],
                        "status" to if (it[StudentFeeTransactions.isApproved]) "Approved" else "Pending",
                        "date" to it[StudentFeeTransactions.timestamp].toString()
                    )
                }
            }
            call.respond(payments)
        }

        post("/approve-payment/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val success = transaction {
                val txn = StudentFeeTransactions.select { StudentFeeTransactions.id eq id }.singleOrNull() ?: return@transaction false
                if (txn[StudentFeeTransactions.isApproved]) return@transaction false

                // Update Transaction
                StudentFeeTransactions.update({ StudentFeeTransactions.id eq id }) {
                    it[isApproved] = true
                    it[reviewedByAdminId] = 1 // Placeholder for current admin
                }

                // Update Balance
                val studentId = txn[StudentFeeTransactions.studentId]
                val amount = txn[StudentFeeTransactions.amount]
                
                StudentFeeBalances.update({ StudentFeeBalances.studentId eq Users.select { Users.id eq studentId }.single()[Users.userId] }) {
                    with(SqlExpressionBuilder) {
                        it.update(amountPaid, amountPaid + amount)
                    }
                }
                true
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.BadRequest)
        }
    }
}
