package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*
import kotlinx.coroutines.delay

import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val chats by viewModel.chats.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopHeader()
            HomeSearchBar()
            ChatList(chats, navController)
        }
        
        FloatingActionButton(
            onClick = { navController.navigate("friends") },
            containerColor = NeonBlue,
            contentColor = PureWhite,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 112.dp) // above bottom bar
        ) {
            Icon(Icons.Filled.AddComment, null)
        }
    }
}

@Composable
fun HomeTopHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar + Status
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Graphite)
                .border(2.dp, NeonBlue, CircleShape)
        ) {
            // Placeholder Avatar
            Icon(
                Icons.Filled.Circle, 
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(12.dp),
                tint = SoftGray
            )
            
            // Online Pulse
            val transition = rememberInfiniteTransition(label = "pulse")
            val alpha by transition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = alpha))
                    .border(2.dp, DeepBlack, CircleShape)
            )
        }

        // App Logo
        Text(
            text = "ChatVerse",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                brush = Brush.linearGradient(listOf(NeonBlue, ElectricPurple))
            )
        )

        // Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = "Search",
                tint = SoftGray,
                modifier = Modifier.size(24.dp)
            )
            Icon(
                Icons.Filled.Notifications,
                contentDescription = "Notifications",
                tint = SoftGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun HomeSearchBar() {
    var query by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(GlassCardBg)
            .border(1.dp, GlassInputBorder, RoundedCornerShape(25.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = SoftGray)
            Spacer(modifier = Modifier.width(12.dp))
            // Minimal fake search field for UI
            Text(
                "Search chats, friends, media...",
                color = SoftGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class ChatItemUiModel(
    val id: String,
    val username: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false
)

@Composable
fun ChatList(chats: List<ChatItemUiModel>, navController: NavController) {
    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        items(chats, key = { it.id }) { chat ->
            ChatItem(
                chat = chat,
                onClick = { navController.navigate("chat/${chat.id}") }
            )
        }
    }
}

@Composable
fun ChatItem(chat: ChatItemUiModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Graphite)
        ) {
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border(2.dp, DeepBlack, CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Chat Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = chat.username,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PureWhite)
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (chat.isTyping) {
                Text(
                    text = "typing...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = NeonBlue, fontWeight = FontWeight.SemiBold)
                )
            } else {
                Text(
                    text = chat.lastMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(color = if (chat.unreadCount > 0) PureWhite else MutedGray),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Meta Info
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = chat.time,
                style = MaterialTheme.typography.labelSmall.copy(color = if (chat.unreadCount > 0) NeonBlue else MutedGray, fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NeonBlue, ElectricPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}
