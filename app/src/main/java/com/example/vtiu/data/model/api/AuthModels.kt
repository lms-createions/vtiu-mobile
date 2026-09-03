package com.example.vtiu.data.model.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginRequest(
    val username: String,
    @SerialName("user_id")
    val userId: String,
    val password: String,
    val role: String = "teacher"
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val user: UserData? = null
)

@Serializable
data class UserData(
    val id: Int,
    @SerialName("user_id") val userId: String,
    val name: String,
    val role: String,
    val department: String? = null,
    @SerialName("profile_picture_url") val profilePictureUrl: String? = null
)
