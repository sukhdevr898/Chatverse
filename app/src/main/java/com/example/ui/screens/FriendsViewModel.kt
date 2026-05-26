package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreService
import com.example.data.FriendRequestData
import com.example.data.UserSession
import com.example.data.toUser
import com.example.data.toFriendRequest
import com.example.data.toFirestore
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

    private val _friendsList = MutableStateFlow<List<UserSearchItem>>(emptyList())
    val friendsList: StateFlow<List<UserSearchItem>> = _friendsList.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var allUsersCache = emptyMap<String, com.example.data.User>()

    init {
        loadData()
    }

    fun loadData() {
        val auth = UserSession.idToken ?: return
        val currentUserId = UserSession.userId ?: return
        
        viewModelScope.launch {
            try {
                val projectId = FirestoreService.getProjectIdFromToken(auth)
                // Load friends
                val friendsResponse = FirestoreService.api.getFriends(projectId, currentUserId, "Bearer $auth")
                if (friendsResponse.isSuccessful) {
                    val friendsMap = friendsResponse.body()?.documents?.mapNotNull { it.name?.substringAfterLast("/") }?.toSet() ?: emptySet()
                    _friends.value = friendsMap
                }
            
                // Load friend requests
                val reqResponse = FirestoreService.api.getFriendRequests(projectId, currentUserId, "Bearer $auth")
                if (reqResponse.isSuccessful) {
                    val newRequests = reqResponse.body()?.documents?.mapNotNull { it.toFriendRequest() } ?: emptyList()
                    _friendRequests.value = newRequests
                }

                // Load users map for searching
                val usersResponse = FirestoreService.api.getUsers(projectId, "Bearer $auth")
                if (usersResponse.isSuccessful) {
                    val docs = usersResponse.body()?.documents ?: emptyList()
                    val usersList = docs.mapNotNull { it.toUser() }
                    allUsersCache = usersList.associateBy { it.id }
                    updateFriendsList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateFriendsList() {
        val currentUserId = UserSession.userId ?: return
        val list = allUsersCache.values.filter { _friends.value.contains(it.id) }.map {
            UserSearchItem(
                id = it.id,
                username = it.username,
                isFriend = true,
                requestSent = false,
                isSelf = it.id == currentUserId
            )
        }.sortedBy { it.username }
        _friendsList.value = list
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
                val projectId = com.example.data.FirestoreService.getProjectIdFromToken(auth)
                val usersResponse = com.example.data.FirestoreService.api.getUsers(projectId, "Bearer $auth")
                
                if (usersResponse.isSuccessful) {
                    val docs = usersResponse.body()?.documents ?: emptyList()
                    val usersList = docs.mapNotNull { it.toUser() }
                    allUsersCache = usersList.associateBy { it.id }
                    _errorMessage.value = null
                } else {
                    val errorBody = usersResponse.errorBody()?.string()
                    _errorMessage.value = "DB Error: ${usersResponse.code()} - $errorBody"
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
                _errorMessage.value = e.message
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
                val projectId = com.example.data.FirestoreService.getProjectIdFromToken(auth)
                com.example.data.FirestoreService.api.sendFriendRequest(projectId, targetUserId, currentUserId, "Bearer $auth", requestData.toFirestore())
                // Mark locally
                val updated = _searchResults.value.map {
                    if (it.id == targetUserId) it.copy(requestSent = true) else it
                }
                _searchResults.value = updated
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    val targetUsername = _searchResults.value.find { it.id == targetUserId }?.username ?: "your friend"
                    android.widget.Toast.makeText(context, "Friend Request Sent to $targetUsername!", android.widget.Toast.LENGTH_SHORT).show()
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
                val projectId = com.example.data.FirestoreService.getProjectIdFromToken(auth)
                val friendDoc = com.example.data.FirestoreDocument(fields = mapOf("isFriend" to com.example.data.FirestoreValue(booleanValue = true)))
                // Add to my friends
                com.example.data.FirestoreService.api.addFriend(projectId, currentUserId, senderId, "Bearer $auth", friendDoc)
                // Add to their friends
                com.example.data.FirestoreService.api.addFriend(projectId, senderId, currentUserId, "Bearer $auth", friendDoc)
                // Remove friend request
                com.example.data.FirestoreService.api.removeFriendRequest(projectId, currentUserId, senderId, "Bearer $auth")
                
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
                val projectId = com.example.data.FirestoreService.getProjectIdFromToken(auth)
                // Remove friend request
                com.example.data.FirestoreService.api.removeFriendRequest(projectId, currentUserId, senderId, "Bearer $auth")
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
