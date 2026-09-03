package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val type: String, // quiz, assignment, exam, grade, fee, general
    val priority: String, // normal, high
    @SerialName("sender_name")
    val senderName: String?,
    @SerialName("sender_type")
    val senderType: String, // system, teacher, admin
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("is_read")
    var isRead: Boolean = false
)
