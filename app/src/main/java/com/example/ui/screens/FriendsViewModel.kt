package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseDatabaseService
import com.example.data.FriendRequestData
import com.example.data.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserSearchItem(
    val id: String,
    val username: String,
    val requestSent: Boolean = false,
    val isFriend: Boolean = false
)

class FriendsViewModel : ViewModel() {
    private val _searchResults = MutableStateFlow<List<UserSearchItem>>(emptyList())
    val searchResults: StateFlow<List<UserSearchItem>> = _searchResults.asStateFlow()

    private val _friendRequests = MutableStateFlow<List<FriendRequestData>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequestData>> = _friendRequests.asStateFlow()
    
    private val _friends = MutableStateFlow<Set<String>>(emptySet())
    val friends: StateFlow<Set<String>> = _friends.asStateFlow()

    private var allUsersCache = emptyMap<String, com.example.data.User>()

    init {
        loadData()
    }

    fun loadData() {
        val auth = UserSession.idToken ?: return
        val currentUserId = UserSession.userId ?: return
        
        viewModelScope.launch {
            try {
                // Load friends
                val friendsResponse = FirebaseDatabaseService.api.getFriends(currentUserId, auth)
                if (friendsResponse.isSuccessful) {
                    val friendsMap = friendsResponse.body() ?: emptyMap()
                    _friends.value = friendsMap.filterValues { it }.keys
                }
            
                // Load friend requests
                val reqResponse = FirebaseDatabaseService.api.getFriendRequests(currentUserId, auth)
                if (reqResponse.isSuccessful) {
                    _friendRequests.value = reqResponse.body()?.values?.toList() ?: emptyList()
                }

                // Load users map for searching
                val usersResponse = FirebaseDatabaseService.api.getUsers(auth)
                if (usersResponse.isSuccessful) {
                    allUsersCache = usersResponse.body() ?: emptyMap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        val currentUserId = UserSession.userId ?: return
        
        val filtered = allUsersCache.values.filter {
            it.id != currentUserId && it.username.contains(query, ignoreCase = true)
        }.map {
            UserSearchItem(
                id = it.id,
                username = it.username,
                isFriend = _friends.value.contains(it.id)
            )
        }
        _searchResults.value = filtered
    }

    fun sendFriendRequest(targetUserId: String, context: android.content.Context) {
        val auth = UserSession.idToken ?: return
        val currentUserId = UserSession.userId ?: return
        val username = UserSession.email?.substringBefore("@") ?: "Unknown User"
        
        viewModelScope.launch {
            try {
                val requestData = FriendRequestData(
                    senderId = currentUserId,
                    senderUsername = username,
                    timestamp = System.currentTimeMillis()
                )
                FirebaseDatabaseService.api.sendFriendRequest(targetUserId, currentUserId, auth, requestData)
                // Mark locally
                val updated = _searchResults.value.map {
                    if (it.id == targetUserId) it.copy(requestSent = true) else it
                }
                _searchResults.value = updated
                
                android.widget.Toast.makeText(context, "Friend Request Sent!", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acceptFriendRequest(senderId: String) {
        val auth = UserSession.idToken ?: return
        val currentUserId = UserSession.userId ?: return

        viewModelScope.launch {
            try {
                // Add to my friends
                FirebaseDatabaseService.api.addFriend(currentUserId, senderId, auth, true)
                // Add to their friends
                FirebaseDatabaseService.api.addFriend(senderId, currentUserId, auth, true)
                // Remove friend request
                FirebaseDatabaseService.api.removeFriendRequest(currentUserId, senderId, auth)
                
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun declineFriendRequest(senderId: String) {
        val auth = UserSession.idToken ?: return
        val currentUserId = UserSession.userId ?: return

        viewModelScope.launch {
            try {
                // Remove friend request
                FirebaseDatabaseService.api.removeFriendRequest(currentUserId, senderId, auth)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
