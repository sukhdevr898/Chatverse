package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreService
import com.example.data.UserSession
import com.example.data.toChatMessage
import com.example.data.toFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatViewModelFactory(private val otherUserId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(otherUserId) as T
    }
}

class ChatViewModel(private val otherUserId: String) : ViewModel() {
    private val _messages = MutableStateFlow<List<MessageUiModel>>(emptyList())
    val messages: StateFlow<List<MessageUiModel>> = _messages.asStateFlow()

    private val api = FirestoreService.api

    // Simple fixed chat node ID between two users
    private val chatId: String
        get() = run {
            val myId = UserSession.userId ?: "unknown_me"
            if (myId < otherUserId) "${myId}_${otherUserId}" else "${otherUserId}_${myId}"
        }

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                loadMessages()
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    private suspend fun loadMessages() {
        val auth = UserSession.idToken ?: return
        try {
            val projectId = FirestoreService.getProjectIdFromToken(auth)
            val response = api.getMessages(projectId, chatId, "Bearer $auth")
            if (response.isSuccessful) {
                val data = response.body()?.documents?.mapNotNull { it.toChatMessage() } ?: emptyList()
                val sorted = data.sortedBy { it.timestamp }
                val myId = UserSession.userId
                _messages.value = sorted.mapIndexed { index, msg ->
                    MessageUiModel(
                        id = index.toString(),
                        text = msg.text,
                        time = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp)),
                        isMine = msg.senderId == myId
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val auth = UserSession.idToken ?: return
        val myId = UserSession.userId ?: return
        viewModelScope.launch {
            try {
                val projectId = FirestoreService.getProjectIdFromToken(auth)
                val payload = com.example.data.ChatMessageData(myId, text, System.currentTimeMillis())
                api.sendMessage(projectId, chatId, "Bearer $auth", payload.toFirestore())
                loadMessages() // reload immediately
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
