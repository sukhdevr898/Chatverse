package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*

@Composable
fun ChatScreen(navController: NavController, chatId: String) {
    // Generate dummy messages
    val messages = listOf(
        MessageUiModel("msg1", "Hello! Are the files ready?", "10:00 AM", false),
        MessageUiModel("msg2", "Yes, just uploading them to the secure node.", "10:02 AM", true),
        MessageUiModel("msg3", "Perfect. Encryption keys attached?", "10:03 AM", false),
        MessageUiModel("msg4", "All set. Sending now.", "10:05 AM", true)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChatHeader(navController, "User $chatId")
            ChatMessagesArea(messages = messages, modifier = Modifier.weight(1f))
            ChatInputArea()
        }
    }
}

@Composable
fun ChatHeader(navController: NavController, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassCardBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Graphite)
                .border(1.dp, NeonBlue, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PureWhite)
            )
            Text(
                text = "Online", 
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF10B981))
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = {  }) {
                Icon(Icons.Filled.Call, contentDescription = "Audio Call", tint = NeonBlue)
            }
            IconButton(onClick = {  }) {
                Icon(Icons.Filled.Videocam, contentDescription = "Video Call", tint = NeonBlue)
            }
            IconButton(onClick = {  }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = SoftGray)
            }
        }
    }
}

data class MessageUiModel(
    val id: String,
    val text: String,
    val time: String,
    val isMine: Boolean
)

@Composable
fun ChatMessagesArea(messages: List<MessageUiModel>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        reverseLayout = true
    ) {
        items(messages.size) { index ->
            // Reversing index since reverseLayout is true
            val msg = messages[messages.size - 1 - index]
            MessageBubble(msg)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MessageBubble(msg: MessageUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (msg.isMine) 20.dp else 4.dp,
                        bottomEnd = if (msg.isMine) 4.dp else 20.dp
                    )
                )
                .background(
                    if (msg.isMine)
                        Brush.linearGradient(listOf(ElectricPurple, NeonBlue))
                    else
                        Brush.linearGradient(listOf(Graphite, SoftDark))
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(horizontalAlignment = if (msg.isMine) Alignment.End else Alignment.Start) {
                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyLarge.copy(color = PureWhite)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg.time,
                    style = MaterialTheme.typography.labelSmall.copy(color = SoftGray, fontSize = 10.sp)
                )
            }
        }
    }
}

@Composable
fun ChatInputArea() {
    var text by remember { mutableStateOf("") }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Attachment */ }) {
            Icon(Icons.Filled.AddCircleOutline, contentDescription = "Add", tint = SoftGray)
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(GlassCardBg)
                .border(1.dp, GlassInputBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (text.isEmpty()) {
                Text("Type a message...", color = SoftGray)
            }
            // Usually represented by BasicTextField in Compose
            Text(text, color = PureWhite) 
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(ElectricPurple, NeonBlue)))
                .clickable { /* Send message */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(if (text.isEmpty()) Icons.Filled.Mic else Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = PureWhite)
        }
    }
}
