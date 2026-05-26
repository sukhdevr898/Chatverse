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
    val isFriend: Boolean = false,
    val isSelf: Boolean = false
)

class FriendsViewModel : ViewModel() {
    private val _searchResults = MutableStateFlow<List<UserSearchItem>>(emptyList())
    val searchResults: StateFlow<List<UserSearchItem>> = _searchResults.asStateFlow()

    private val _friendRequests = MutableStateFlow<List<FriendRequestData>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequestData>> = _friendRequests.asStateFlow()
    
    private val _friends = MutableStateFlow<Set<String>>(emptySet())
    val friends: StateFlow<Set<String>> = _friends.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

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
                    val newRequests = reqResponse.body()?.values?.toList() ?: emptyList()
                    val oldRequests = _friendRequests.value
                    _friendRequests.value = newRequests
                    
                    // Show notification for new requests
                    val newOnes = newRequests.filter { newReq -> oldRequests.none { it.senderId == newReq.senderId } }
                    if (newOnes.isNotEmpty()) {
                        val notificationContext = UserSession.appContext
                        if (notificationContext != null) {
                            newOnes.forEach { req ->
                                com.example.NotificationHelper.showSystemNotification(
                                    notificationContext,
                                    "New Friend Request",
                                    "${req.senderUsername} sent you a friend request!"
                                )
                            }
                        }
                    }
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
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        val currentUserId = UserSession.userId ?: return
        val auth = UserSession.idToken ?: return
        
        viewModelScope.launch {
            _isSearching.value = true
            try {
                // Fetch fresh users to ensure we have everyone
                val usersResponse = FirebaseDatabaseService.api.getUsers(auth)
                if (usersResponse.isSuccessful) {
                    allUsersCache = usersResponse.body() ?: emptyMap()
                } else {
                    val errorBody = usersResponse.errorBody()?.string()
                    println("Search Failed: $errorBody")
                }
                
                val filtered = allUsersCache.values.filter {
                    it.username.contains(cleanQuery, ignoreCase = true)
                }.map {
                    UserSearchItem(
                        id = it.id,
                        username = it.username,
                        isFriend = _friends.value.contains(it.id),
                        requestSent = _friendRequests.value.any { req -> req.senderId == it.id },
                        isSelf = it.id == currentUserId
                    )
                }
                _searchResults.value = filtered
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearching.value = false
            }
        }
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
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    val targetUsername = _searchResults.value.find { it.id == targetUserId }?.username ?: "your friend"
                    android.widget.Toast.makeText(context, "Friend Request Sent!", android.widget.Toast.LENGTH_SHORT).show()
                    com.example.NotificationHelper.showSystemNotification(
                        context, 
                        "Friend Request Sent", 
                        "Notification sent to $targetUsername that you've added them!"
                    )
                }
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
