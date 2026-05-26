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
        containerColor = MaterialTheme.colorScheme.background,
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
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                 androidx.compose.foundation.Image(
                     painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_launcher_foreground), 
                     contentDescription = "Contact Avatar",
                     modifier = Modifier.fillMaxSize()
                 )
                 // Online dot
                 Box(
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .size(12.dp)
                         .clip(CircleShape)
                         .background(MaterialTheme.colorScheme.tertiary)
                         .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                 )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 0.2.sp),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "Online", 
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Videocam, 
                contentDescription = "Video Call", 
                tint = MaterialTheme.colorScheme.onSurface, 
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { }
                    .padding(8.dp)
            )
            Icon(
                Icons.Filled.Call, 
                contentDescription = "Audio Call", 
                tint = MaterialTheme.colorScheme.onSurface, 
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { }
                    .padding(8.dp)
            )
            Box {
                Icon(
                    Icons.Filled.MoreVert, 
                    contentDescription = "Options", 
                    tint = MaterialTheme.colorScheme.onSurface, 
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { showMenu = !showMenu }
                        .padding(8.dp)
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Search", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                    DropdownMenuItem(
                        text = { Text("Mute notifications", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Filled.NotificationsOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear chat", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Filled.DeleteSweep, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                    DropdownMenuItem(
                        text = { Text("Block user", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Filled.Block, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
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
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
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
                        Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                    else
                        Modifier.background(MaterialTheme.colorScheme.surface)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyLarge.copy(color = if (msg.isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, lineHeight = 21.sp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = msg.time,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            )
            if (msg.isMine) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.DoneAll,
                    contentDescription = "Read",
                    tint = MaterialTheme.colorScheme.primary,
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
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .imePadding()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Attachment */ }) {
            Icon(Icons.Filled.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text("Message", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    innerTextField()
                }
            )
            IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.AttachFile, contentDescription = "Attach", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.SentimentSatisfied, contentDescription = "Emoji", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                .clickable {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(if (text.isEmpty()) Icons.Filled.Mic else Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
