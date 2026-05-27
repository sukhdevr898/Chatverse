package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.FriendRequestData
import com.example.ui.theme.*

@Composable
fun FriendsScreen(navController: NavController, viewModel: FriendsViewModel = viewModel()) {
    val searchResults by viewModel.searchResults.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val friendsList by viewModel.friendsList.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Friends") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        viewModel.loadData()
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // App Header for Friends
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF3F4F6))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Friends",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color(0xFF111827)
                )
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = query,
                        onValueChange = { 
                            query = it
                            if (it.isNotEmpty()) viewModel.searchUsers(it)
                        },
                        singleLine = true,
                        placeholder = {
                            Text("Find friends...", color = Color.Gray, fontSize = 14.sp)
                        },
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
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            cursorColor = Color(0xFFA855F7)
                        ),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    )
                }
            }

            if (query.isNotEmpty()) {
                // Search Results State
                SectionHeader(title = "Search Results")
                if (isSearching) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFA855F7))
                    }
                } else if (searchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No users found", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))) {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    searchResults.forEachIndexed { index, user ->
                                        UserSearchItemCard(user, onAddFriend = { viewModel.sendFriendRequest(user.id, context) })
                                        if (index < searchResults.size - 1) {
                                            HorizontalDivider(color = Color(0xFFF9FAFB), modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Default State (Chips + Lists)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            label = "All Friends (${friendsList.size})",
                            isSelected = selectedFilter == "All Friends",
                            onClick = { selectedFilter = "All Friends" }
                        )
                    }
                    item {
                        FilterChip(
                            label = "Requests (${friendRequests.size})",
                            isSelected = selectedFilter == "Requests",
                            onClick = { selectedFilter = "Requests" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedFilter == "All Friends") {
                    SectionHeader(title = "My Contacts")
                    
                    if (friendsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No friends yet", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))) {
                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        friendsList.forEachIndexed { index, friend ->
                                            FriendContactCard(friend, onChatClick = {
                                                val safeUsername = friend.username.ifEmpty { "unknown" }
                                                val encodedUsername = android.net.Uri.encode(safeUsername)
                                                navController.navigate("chat/${friend.id}/$encodedUsername")
                                            })
                                            if (index < friendsList.size - 1) {
                                                HorizontalDivider(color = Color(0xFFF9FAFB), modifier = Modifier.padding(horizontal = 16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedFilter == "Requests") {
                    SectionHeader(title = "Pending Requests")
                    
                    if (friendRequests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No pending requests", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                            contentPadding = PaddingValues(bottom = 120.dp)
                        ) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))) {
                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        friendRequests.forEachIndexed { index, req ->
                                            FriendRequestActionCard(
                                                req = req,
                                                onAccept = { viewModel.acceptFriendRequest(req.senderId) },
                                                onDecline = { viewModel.declineFriendRequest(req.senderId) }
                                            )
                                            if (index < friendRequests.size - 1) {
                                                HorizontalDivider(color = Color(0xFFF9FAFB), modifier = Modifier.padding(horizontal = 16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Color(0xFF111827) 
                else Color.White
            )
            .border(
                1.dp, 
                if (isSelected) Color.Transparent else Color(0xFFF3F4F6), 
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}

@Composable
fun FriendContactCard(user: UserSearchItem, onChatClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onChatClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username,
                fontWeight = FontWeight.ExtraBold, 
                color = Color(0xFF111827),
                fontSize = 16.sp
            )
        }
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Chat, 
                contentDescription = "Chat", 
                tint = Color(0xFF111827),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun UserSearchItemCard(user: UserSearchItem, onAddFriend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username,
                fontWeight = FontWeight.ExtraBold, 
                color = Color(0xFF111827),
                fontSize = 16.sp
            )
        }
        
        if (user.isSelf) {
            Text("(You)", color = Color.Gray, fontSize = 14.sp)
        } else if (user.isFriend) {
            Icon(Icons.Filled.Check, contentDescription = "Friends", tint = Color(0xFF10B981))
        } else if (user.requestSent) {
            Text("Sent", color = Color.Gray, fontSize = 14.sp)
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF111827))
                    .clickable { onAddFriend() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun FriendRequestActionCard(req: FriendRequestData, onAccept: () -> Unit, onDecline: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
             Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = req.senderUsername,
                fontWeight = FontWeight.ExtraBold, 
                color = Color(0xFF111827),
                fontSize = 16.sp
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .clickable { onAccept() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .clickable { onDecline() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
