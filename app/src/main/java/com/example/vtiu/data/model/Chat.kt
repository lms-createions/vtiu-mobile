package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatParticipant(
    @SerialName("user_public_id")
    val userPublicId: String,
    val role: String,
    val name: String,
    @SerialName("profile_picture")
    val profilePicture: String? = null
)

@Serializable
data class ChatMessage(
    val id: Int,
    @SerialName("conversation_id")
    val conversationId: Int,
    @SerialName("sender_public_id")
    val senderPublicId: String,
    @SerialName("sender_role")
    val senderRole: String,
    @SerialName("sender_name")
    val senderName: String,
    val content: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false
)

@Serializable
data class ChatConversation(
    val id: Int,
    val type: String, // direct | broadcast | class
    val name: String?,
    val participants: List<ChatParticipant>,
    @SerialName("last_message")
    val lastMessage: ChatMessage?,
    @SerialName("unread_count")
    val unreadCount: Int,
    @SerialName("updated_at")
    val updatedAt: String
)
