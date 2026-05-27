package com.example.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FriendRequestData
import com.example.ui.theme.*

@Composable
fun HomeScreen(navController: NavController, onNavigateToFriends: () -> Unit, viewModel: HomeViewModel = viewModel(), friendsViewModel: FriendsViewModel = viewModel()) {
    val chats by viewModel.chats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val friendRequests by friendsViewModel.friendRequests.collectAsState()

    LaunchedEffect(Unit) {
        friendsViewModel.loadData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopHeader()
            HomeSearchBar(searchQuery, viewModel::updateSearchQuery)
            
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                if (friendRequests.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        SectionHeader(
                            title = "Received Requests", 
                            badgeCount = friendRequests.size,
                            actionText = "See all", 
                            onActionClick = onNavigateToFriends
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(friendRequests) { req ->
                                FriendRequestCardHome(
                                    req = req,
                                    onAdd = { friendsViewModel.acceptFriendRequest(req.senderId) },
                                    onHide = { friendsViewModel.declineFriendRequest(req.senderId) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
                item {
                    SectionHeader(
                        title = "Recent Chats", 
                        badgeCount = 0,
                        actionText = "", 
                        onActionClick = {}
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (chats.isEmpty()) {
                    item {
                        EmptyChatsFallback()
                    }
                } else {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                chats.forEachIndexed { index, chat ->
                                    ChatItem(
                                        chat = chat,
                                        onClick = { 
                                            viewModel.resetUnreadCount(chat.id)
                                            val safeUsername = chat.username.ifEmpty { "unknown" }
                                            val encodedUsername = android.net.Uri.encode(safeUsername)
                                            navController.navigate("chat/${chat.id}/$encodedUsername") 
                                        }
                                    )
                                    if (index < chats.size - 1) {
                                        HorizontalDivider(color = Color(0xFFF9FAFB), modifier = Modifier.padding(horizontal = 16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
                .size(64.dp)
                .background(Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF3B82F6))), CircleShape)
                .clickable { onNavigateToFriends() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Chat, contentDescription = "New Chat", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun HomeTopHeader() {
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
            text = "ChatVerse",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color(0xFF111827)
            )
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR Code Scanner Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF3F4F6), CircleShape)
                    .clickable {  },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.QrCodeScanner,
                    contentDescription = "Scan",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6))
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.fillMaxSize().padding(8.dp))
            }
        }
    }
}

@Composable
fun HomeSearchBar(query: String, onQueryChange: (String) -> Unit) {
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
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Search friends, messages...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
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
}

@Composable
fun SectionHeader(title: String, badgeCount: Int = 0, actionText: String = "", onActionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Gray
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
        
        if (actionText.isNotEmpty()) {
            Text(
                text = actionText,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA855F7),
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
fun FriendRequestCardHome(req: FriendRequestData, onAdd: () -> Unit, onHide: () -> Unit) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6))
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.fillMaxSize().padding(12.dp))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = req.senderUsername,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF111827),
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Suggested for you",
            color = Color.Gray,
            fontSize = 11.sp,
            maxLines = 1
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAdd,
                modifier = Modifier.weight(1f).height(32.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
            ) {
                Text("Add", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onHide,
                modifier = Modifier.weight(1f).height(32.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Text("Hide", color = Color(0xFF111827), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyChatsFallback() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Chat,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No recent chats",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

data class ChatItemUiModel(
    val id: String,
    val username: String,
    val lastMessage: String,
    val time: String,
    val timestamp: Long = 0L,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false
)

@Composable
fun ChatItem(chat: ChatItemUiModel, onClick: () -> Unit) {
    val hasUnread = chat.unreadCount > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 0.dp, y = 0.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Chat Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.username,
                    fontWeight = FontWeight.ExtraBold, 
                    color = Color(0xFF111827),
                    fontSize = 16.sp
                )
                Text(
                    text = chat.time,
                    color = if (hasUnread) Color(0xFFA855F7) else Color.Gray,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (chat.isTyping) "typing..." else chat.lastMessage,
                    color = if (hasUnread || chat.isTyping) Color(0xFF111827) else Color.Gray,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 20.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFA855F7))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                            fontWeight = FontWeight.Bold, 
                            color = Color.White, 
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
