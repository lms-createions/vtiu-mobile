package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VClassMaterial(
    val id: Int,
    val title: String,
    @SerialName("course_name")
    val courseName: String,
    val filename: String,
    @SerialName("file_type")
    val fileType: String, // pdf, image, video, doc
    @SerialName("uploaded_at")
    val uploadedAt: String
)

@Serializable
data class VClassAssignment(
    val id: Int,
    val title: String,
    @SerialName("course_name")
    val courseName: String,
    val instructions: String?,
    @SerialName("due_date")
    val dueDate: String,
    val filename: String? = null
)

@Serializable
data class VClassMeeting(
    val id: Int,
    val title: String,
    @SerialName("course_name")
    val courseName: String,
    @SerialName("teacher_name")
    val teacherName: String,
    @SerialName("scheduled_start")
    val scheduledStart: String,
    @SerialName("scheduled_end")
    val scheduledEnd: String,
    @SerialName("join_url")
    val joinUrl: String?,
    @SerialName("is_live")
    val isLive: Boolean = false,
    @SerialName("requires_approval")
    val requiresApproval: Boolean = false
)

@Serializable
data class VClassRecording(
    val id: Int,
    val title: String,
    @SerialName("course_name")
    val courseName: String,
    @SerialName("teacher_name")
    val teacherName: String,
    @SerialName("recorded_at")
    val recordedAt: String,
    val duration: String,
    val thumbnail: String? = null,
    val url: String
)
