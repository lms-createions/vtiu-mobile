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
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun Route.appointmentRoutes() {
    route("/api/appointments") {
        get("/slots") {
            val slots = transaction {
                (AppointmentSlots innerJoin TeacherProfiles innerJoin Users).selectAll().where { AppointmentSlots.isBooked eq false }.map {
                    AppointmentSlotApi(
                        id = it[AppointmentSlots.id],
                        teacherName = "${it[Users.firstName]} ${it[Users.lastName]}",
                        date = it[AppointmentSlots.date],
                        startTime = it[AppointmentSlots.startTime],
                        endTime = it[AppointmentSlots.endTime]
                    )
                }
            }
            call.respond(slots)
        }

        get("/my-bookings/{userId}") {
            val userId = call.parameters["userId"] ?: ""
            val bookings = transaction {
                val studentRow = StudentProfiles.selectAll().where { StudentProfiles.userId eq userId }.singleOrNull() ?: return@transaction emptyList<AppointmentBookingApi>()
                (AppointmentBookings innerJoin AppointmentSlots innerJoin TeacherProfiles innerJoin Users).selectAll().where { 
                    AppointmentBookings.studentId eq studentRow[StudentProfiles.id] 
                }.map {
                    AppointmentBookingApi(
                        id = it[AppointmentBookings.id],
                        teacherName = "${it[Users.firstName]} ${it[Users.lastName]}",
                        title = "Consultation", 
                        date = it[AppointmentSlots.date],
                        startTime = it[AppointmentSlots.startTime],
                        endTime = it[AppointmentSlots.endTime],
                        status = it[AppointmentBookings.status]
                    )
                }
            }
            call.respond(bookings)
        }

        post("/book") {
            val request = call.receive<Map<String, String>>()
            val userId = request["student_id"] ?: ""
            val slotId = request["slot_id"]?.toIntOrNull() ?: 0
            val note = request["note"] ?: ""

            val success = transaction {
                val studentRow = StudentProfiles.selectAll().where { StudentProfiles.userId eq userId }.singleOrNull() ?: return@transaction false
                val slot = AppointmentSlots.selectAll().where { AppointmentSlots.id eq slotId }.singleOrNull() ?: return@transaction false
                if (slot[AppointmentSlots.isBooked]) return@transaction false

                AppointmentBookings.insert {
                    it[studentId] = studentRow[StudentProfiles.id]
                    it[AppointmentBookings.slotId] = slotId
                    it[status] = "pending"
                    it[AppointmentBookings.note] = note
                    it[requestedOn] = LocalDateTime.now().toKotlinLocalDateTime()
                }

                AppointmentSlots.update({ AppointmentSlots.id eq slotId }) {
                    it[isBooked] = true
                }
                true
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.BadRequest)
        }

        post("/update_status") {
            val request = call.receive<Map<String, String>>()
            val bookingId = request["booking_id"]?.toIntOrNull() ?: 0
            val status = request["status"] ?: ""

            val success = transaction {
                AppointmentBookings.update({ AppointmentBookings.id eq bookingId }) {
                    it[AppointmentBookings.status] = status
                } > 0
            }
            if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.BadRequest)
        }
    }
}
