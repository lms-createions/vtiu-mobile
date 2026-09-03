package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentSlot(
    val id: Int,
    @SerialName("teacher_name")
    val teacherName: String,
    val date: String,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("end_time")
    val endTime: String,
    @SerialName("is_booked")
    val isBooked: Boolean = false
)

@Serializable
data class AppointmentBooking(
    val id: Int,
    val slot: AppointmentSlot,
    val status: String, // Pending, Approved, Completed, Cancelled
    val note: String? = null,
    @SerialName("created_at")
    val createdAt: String
)
