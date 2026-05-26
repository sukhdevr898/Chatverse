package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreService
import com.example.data.UserSession
import com.example.data.toUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _chats = MutableStateFlow<List<ChatItemUiModel>>(emptyList())
    val chats: StateFlow<List<ChatItemUiModel>> = _chats.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allChats = listOf<ChatItemUiModel>()

    init {
        loadUsers()
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterChats()
    }
    
    private fun filterChats() {
        val q = _searchQuery.value.trim().lowercase()
        if (q.isEmpty()) {
            _chats.value = allChats
        } else {
            _chats.value = allChats.filter { it.username.lowercase().contains(q) }
        }
    }

    private fun loadUsers() {
        val token = UserSession.idToken ?: return
        val currentUserId = UserSession.userId ?: return
        viewModelScope.launch {
            try {
                val projectId = FirestoreService.getProjectIdFromToken(token)
                // Get friends first
                val friendsResponse = FirestoreService.api.getFriends(projectId, currentUserId, "Bearer $token")
                val friendsList = if (friendsResponse.isSuccessful) {
                    friendsResponse.body()?.documents?.mapNotNull { it.name?.substringAfterLast("/") }?.toSet() ?: emptySet()
                } else emptySet()

                val response = FirestoreService.api.getUsers(projectId, "Bearer $token")
                if (response.isSuccessful) {
                    val usersList = response.body()?.documents?.mapNotNull { it.toUser() } ?: emptyList()
                    val chatItems = usersList
                        .filter { friendsList.contains(it.id) }
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
                    allChats = chatItems
                    filterChats()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
