package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreService
import com.example.data.UserSession
import com.example.data.toUser
import com.example.data.toChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _chats = MutableStateFlow<List<ChatItemUiModel>>(emptyList())
    val chats: StateFlow<List<ChatItemUiModel>> = _chats.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allChats = listOf<ChatItemUiModel>()

    private var isFirstPoll = true
    private var lastSeenMessageTimestamps = mutableMapOf<String, Long>()
    private val unreadCounts = mutableMapOf<String, Int>()

    init {
        startPolling()
    }
    
    fun resetUnreadCount(userId: String) {
        unreadCounts[userId] = 0
        allChats = allChats.map { if (it.id == userId) it.copy(unreadCount = 0) else it }
        filterChats()
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
    
    private fun startPolling() {
        viewModelScope.launch {
            while(isActive) {
                loadUsersAndMessages()
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    private suspend fun loadUsersAndMessages() {
        val token = UserSession.idToken ?: return
        val currentUserId = UserSession.userId ?: return
        
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
                val friendsData = usersList.filter { friendsList.contains(it.id) }
                
                val chatItems = mutableListOf<ChatItemUiModel>()
                
                for (user in friendsData) {
                    val chatId = if (currentUserId < user.id) "${currentUserId}_${user.id}" else "${user.id}_${currentUserId}"
                    val msgResp = FirestoreService.api.getMessages(projectId, chatId, "Bearer $token")
                    
                    var lastMsgText = "Tap to chat"
                    var lastTime = ""
                    var unread = 0
                    var timestamp = 0L
                    
                    if (msgResp.isSuccessful) {
                        val msgs = msgResp.body()?.documents?.mapNotNull { it.toChatMessage() } ?: emptyList()
                        if (msgs.isNotEmpty()) {
                            val sorted = msgs.sortedBy { it.timestamp }
                            val latest = sorted.last()
                            timestamp = latest.timestamp
                            lastMsgText = latest.text
                            lastTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(latest.timestamp))
                            
                            if (isFirstPoll) {
                                lastSeenMessageTimestamps[user.id] = latest.timestamp
                            } else {
                                val prevTime = lastSeenMessageTimestamps[user.id] ?: 0L
                                val newMsgsCount = sorted.count { it.senderId != currentUserId && it.timestamp > prevTime }
                                if (newMsgsCount > 0) {
                                    val context = UserSession.appContext
                                    if (context != null) {
                                        com.example.NotificationHelper.showSystemNotification(context, user.username, latest.text)
                                    }
                                    val currentUnread = unreadCounts[user.id] ?: 0
                                    unreadCounts[user.id] = currentUnread + newMsgsCount
                                }
                                lastSeenMessageTimestamps[user.id] = latest.timestamp
                            }
                        }
                    }
                    
                    unread = unreadCounts[user.id] ?: 0
                    
                    chatItems.add(
                        ChatItemUiModel(
                            id = user.id,
                            username = user.username,
                            lastMessage = lastMsgText,
                            time = lastTime,
                            timestamp = timestamp,
                            unreadCount = unread,
                            isOnline = user.isOnline,
                            isTyping = false
                        )
                    )
                }
                
                isFirstPoll = false
                
                allChats = chatItems.sortedByDescending { it.timestamp }
                filterChats()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
