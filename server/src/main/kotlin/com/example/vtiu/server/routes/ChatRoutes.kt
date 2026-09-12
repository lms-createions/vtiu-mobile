package com.example.vtiu.server.routes

import com.example.vtiu.server.models.*
import com.example.vtiu.server.db.*
import com.example.vtiu.server.redis.RedisFactory
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import redis.clients.jedis.JedisPubSub
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

val chatSessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
private const val REDIS_CHAT_CHANNEL = "vtiu_chat_broadcast"

fun initChatRedisSubscriber() {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            RedisFactory.getJedis().use { jedis ->
                println("Redis: Chat Subscriber started on channel $REDIS_CHAT_CHANNEL")
                jedis.subscribe(object : JedisPubSub() {
                    override fun onMessage(channel: String, message: String) {
                        val msg = Json.decodeFromString<ChatMessageApi>(message)
                        broadcastToLocalSessions(msg)
                    }
                }, REDIS_CHAT_CHANNEL)
            }
        } catch (e: Exception) {
            println("Redis: Chat Subscriber Error: ${e.message}")
        }
    }
}

private fun broadcastToLocalSessions(msg: ChatMessageApi) {
    val msgJson = Json.encodeToString(msg)
    if (msg.receiverId == "global" || msg.receiverId.startsWith("meeting_")) {
        chatSessions.values.forEach { session ->
            GlobalScope.launch {
                try {
                    session.send(Frame.Text(msgJson))
                } catch (e: Exception) {}
            }
        }
    } else {
        chatSessions[msg.receiverId]?.let { session ->
            GlobalScope.launch {
                try {
                    session.send(Frame.Text(msgJson))
                } catch (e: Exception) {}
            }
        }
        // Also echo to sender if they are on this instance
        chatSessions[msg.senderId]?.let { session ->
            GlobalScope.launch {
                try {
                    session.send(Frame.Text(msgJson))
                } catch (e: Exception) {}
            }
        }
    }
}

fun Route.chatRoutes() {
    route("/api/chat") {
        get("/history/{receiverId}") {
            val receiverId = call.parameters["receiverId"] ?: "global"
            val history = transaction {
                ChatMessages.selectAll().where { ChatMessages.receiverId eq receiverId }
                    .orderBy(ChatMessages.timestamp, SortOrder.ASC)
                    .map {
                        val sender = Users.selectAll().where { Users.userId eq it[ChatMessages.senderId] }.singleOrNull()
                        ChatMessageApi(
                            id = it[ChatMessages.id],
                            senderId = it[ChatMessages.senderId],
                            senderName = if (sender != null) "${sender[Users.firstName]} ${sender[Users.lastName]}" else "Unknown",
                            receiverId = it[ChatMessages.receiverId],
                            message = it[ChatMessages.message],
                            timestamp = it[ChatMessages.timestamp].toString(),
                            isRead = it[ChatMessages.isRead]
                        )
                    }
            }
            call.respond(history)
        }

        webSocket("/ws/{userId}") {
            val userId = call.parameters["userId"] ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No User ID"))
            chatSessions[userId] = this
            println("Chat: User $userId connected. Total active: ${chatSessions.size}")

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        val msgRequest = Json.decodeFromString<ChatMessageApi>(text)
                        
                        // Save to Database
                        val savedMsg = transaction {
                            val id = ChatMessages.insert {
                                it[senderId] = userId
                                it[receiverId] = msgRequest.receiverId
                                it[message] = msgRequest.message
                                it[timestamp] = LocalDateTime.now().toKotlinLocalDateTime()
                            } get ChatMessages.id

                            val sender = Users.selectAll().where { Users.userId eq userId }.singleOrNull()
                            ChatMessageApi(
                                id = id,
                                senderId = userId,
                                senderName = if (sender != null) "${sender[Users.firstName]} ${sender[Users.lastName]}" else "User",
                                receiverId = msgRequest.receiverId,
                                message = msgRequest.message,
                                timestamp = LocalDateTime.now().toString(),
                                isRead = false
                            )
                        }

                        // Broadcast via Redis
                        val msgJson = Json.encodeToString(savedMsg)
                        RedisFactory.getJedis().use { it.publish(REDIS_CHAT_CHANNEL, msgJson) }
                    }
                }
            } catch (e: Exception) {
                println("Chat error for $userId: ${e.message}")
            } finally {
                chatSessions.remove(userId)
                println("Chat: User $userId disconnected.")
            }
        }
    }
}
