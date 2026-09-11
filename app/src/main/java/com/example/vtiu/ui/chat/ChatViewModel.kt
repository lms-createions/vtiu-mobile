package com.example.vtiu.ui.chat

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vtiu.data.model.api.ChatMessageApi
import com.example.vtiu.data.repository.LmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: LmsRepository,
    private val client: HttpClient,
    @Named("baseUrl") private val baseUrl: String
) : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessageApi>()
    val messages: List<ChatMessageApi> = _messages

    private val _isConnected = mutableStateOf(false)
    val isConnected: State<Boolean> = _isConnected

    private var session: DefaultClientWebSocketSession? = null

    fun connect(userId: String) {
        viewModelScope.launch {
            try {
                // 1. Fetch History
                val history = repository.getChatHistory("global")
                _messages.clear()
                _messages.addAll(history)

                // 2. Connect WebSocket
                val wsHost = baseUrl.substringAfter("://").substringBefore("/")
                val isSecure = baseUrl.startsWith("https")
                
                client.webSocket(
                    method = HttpMethod.Get,
                    host = wsHost,
                    port = if (isSecure) 443 else 80,
                    path = "/api/chat/ws/$userId"
                ) {
                    session = this
                    _isConnected.value = true
                    println("Chat: WebSocket connected to $wsHost")
                    
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val msg = Json.decodeFromString<ChatMessageApi>(frame.readText())
                            _messages.add(msg)
                        }
                    }
                }
            } catch (e: Exception) {
                println("Chat Connection Error: ${e.message}")
                e.printStackTrace()
                _isConnected.value = false
            }
        }
    }

    fun sendMessage(senderId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                val msg = ChatMessageApi(
                    senderId = senderId,
                    receiverId = "global",
                    message = text
                )
                val json = Json.encodeToString(msg)
                session?.send(Frame.Text(json))
            } catch (e: Exception) {
                println("Send Error: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            session?.close()
        }
    }
}
