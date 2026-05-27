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

import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen(navController: NavController, chatId: String, username: String) {
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(chatId))
    val messages by viewModel.messages.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF8F9FA),
        topBar = { ChatHeader(navController, username) },
        bottomBar = { ChatInputArea { viewModel.sendMessage(it) } }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ChatMessagesArea(
                messages = messages,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ChatHeader(navController: NavController, title: String) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFF3F4F6))
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF111827))
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Row(
            modifier = Modifier.weight(1f).clickable { },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6))
            ) {
                 Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.fillMaxSize().padding(8.dp))
                 // Online dot
                 Box(
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .size(12.dp)
                         .clip(CircleShape)
                         .background(Color(0xFF10B981))
                         .border(2.dp, Color.White, CircleShape)
                 )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = Color(0xFF111827), 
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "Online", 
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF10B981),
                    fontSize = 12.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Videocam, contentDescription = "Video Call", tint = Color.Gray, modifier = Modifier.size(20.dp).clickable { })
            }
            Box(modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Call, contentDescription = "Audio Call", tint = Color.Gray, modifier = Modifier.size(20.dp).clickable { })
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
            val msg = messages[messages.size - 1 - index]
            MessageBubble(msg)
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Today",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .background(Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MessageBubble(msg: MessageUiModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isMine) Alignment.End else Alignment.Start
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
                .then(
                    if (msg.isMine)
                        Modifier.background(Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF3B82F6))))
                    else
                        Modifier.background(Color.White).border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 20.dp
                        ))
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = msg.text,
                fontSize = 15.sp,
                color = if (msg.isMine) Color.White else Color(0xFF111827), 
                lineHeight = 21.sp
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = msg.time,
                color = Color.Gray, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Bold
            )
            if (msg.isMine) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.DoneAll,
                    contentDescription = "Read",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ChatInputArea(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFF3F4F6))
            .imePadding()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape).clickable { }, contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.Gray)
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF3F4F6))
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = LocalTextStyle.current.copy(color = Color(0xFF111827), fontSize = 15.sp),
                modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text("Message...", color = Color.Gray, fontSize = 15.sp)
                    }
                    innerTextField()
                }
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF3B82F6))))
                .clickable {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp).offset(x = 2.dp))
        }
    }
}
