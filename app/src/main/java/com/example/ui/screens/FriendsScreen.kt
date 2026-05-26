package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.FriendRequestData
import com.example.ui.theme.*

@Composable
fun FriendsScreen(navController: NavController, viewModel: FriendsViewModel = viewModel()) {
    val searchResults by viewModel.searchResults.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        viewModel.loadData()
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Friends",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = PureWhite),
                modifier = Modifier.padding(24.dp)
            )

            // Search / Add Friend Field
            var query by remember { mutableStateOf("") }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassCardBg)
                    .border(1.dp, GlassInputBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        viewModel.searchUsers(query)
                        keyboardController?.hide()
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = NeonBlue)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = query,
                        onValueChange = { 
                            query = it
                        },
                        singleLine = true,
                        placeholder = { Text("Add Friend by Username...", color = SoftGray) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = { 
                                viewModel.searchUsers(query)
                                keyboardController?.hide()
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (query.isNotEmpty()) {
                Text(
                    "Search Results",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = PureWhite),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                if (searchResults.isEmpty()) {
                    Text(
                        "No users found",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SoftGray),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchResults) { user ->
                            UserSearchCard(user) { viewModel.sendFriendRequest(user.id, context) }
                        }
                    }
                }
            } else {
                Text(
                    "Friend Requests",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = PureWhite),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                if (friendRequests.isEmpty()) {
                    Text(
                        "No pending requests",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SoftGray),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(friendRequests) { req ->
                            FriendRequestCard(req, 
                                onAccept = { viewModel.acceptFriendRequest(req.senderId) },
                                onDecline = { viewModel.declineFriendRequest(req.senderId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchCard(user: UserSearchItem, onAddFriend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassCardBg)
            .border(1.dp, GlassInputBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(StitchGradient3, ElectricPurple)))
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PureWhite)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (user.isFriend) {
            Icon(Icons.Filled.Check, contentDescription = "Friends", tint = NeonBlue)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Friends", color = NeonBlue, style = MaterialTheme.typography.labelLarge)
        } else if (user.requestSent) {
            Text("Request Sent", color = SoftGray, style = MaterialTheme.typography.labelLarge)
        } else {
            // Add Friend action
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonBlue.copy(alpha = 0.2f))
                    .clickable { onAddFriend() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = "Add Friend", tint = NeonBlue)
            }
        }
    }
}

@Composable
fun FriendRequestCard(req: FriendRequestData, onAccept: () -> Unit, onDecline: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassCardBg)
            .border(1.dp, GlassInputBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(StitchGradient3, ElectricPurple)))
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = req.senderUsername,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PureWhite)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Accept
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .clickable { onAccept() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Accept", tint = Color(0xFF10B981))
            }
            // Decline
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                    .clickable { onDecline() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Decline", tint = Color(0xFFEF4444))
            }
        }
    }
}
