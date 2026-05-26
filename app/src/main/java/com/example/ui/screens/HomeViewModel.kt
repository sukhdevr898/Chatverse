package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseDatabaseService
import com.example.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _chats = MutableStateFlow<List<ChatItemUiModel>>(emptyList())
    val chats: StateFlow<List<ChatItemUiModel>> = _chats.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        val token = UserSession.idToken ?: return
        val currentUserId = UserSession.userId ?: return
        viewModelScope.launch {
            try {
                val response = FirebaseDatabaseService.api.getUsers(token)
                if (response.isSuccessful) {
                    val usersMap = response.body() ?: emptyMap()
                    val chatItems = usersMap.values
                        .filter { it.id != currentUserId }
                        .map { user ->
                            ChatItemUiModel(
                                id = user.id,
                                username = user.username,
                                lastMessage = "Tap to chat",
                                time = "",
                                unreadCount = 0,
                                isOnline = user.isOnline,
                                isTyping = false
                            )
                        }
                    _chats.value = chatItems
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
