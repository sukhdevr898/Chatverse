package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*

@Composable
fun CallsScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // App Header for Calls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calls",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                    )
                )
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        placeholder = {
                            Text("Search call history...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    )
                }
            }

            SectionHeader(title = "Recent Calls")

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    CallHistoryItem(
                        name = "Vikram Singh",
                        time = "10:30 AM",
                        type = CallType.MISSED_AUDIO
                    )
                }
                item {
                    CallHistoryItem(
                        name = "Chhaya",
                        time = "Yesterday",
                        type = CallType.OUTGOING_VIDEO
                    )
                }
                item {
                    CallHistoryItem(
                        name = "Java Goat Devs",
                        time = "Sunday",
                        type = CallType.INCOMING_AUDIO
                    )
                }
            }
        }
    }
}

enum class CallType { INCOMING_AUDIO, OUTGOING_AUDIO, MISSED_AUDIO, INCOMING_VIDEO, OUTGOING_VIDEO, MISSED_VIDEO }

@Composable
fun CallHistoryItem(name: String, time: String, type: CallType) {
    val isMissed = type == CallType.MISSED_AUDIO || type == CallType.MISSED_VIDEO
    val isVideo = type == CallType.INCOMING_VIDEO || type == CallType.OUTGOING_VIDEO || type == CallType.MISSED_VIDEO
    val isIncoming = type == CallType.INCOMING_AUDIO || type == CallType.INCOMING_VIDEO
    
    val iconColor = when {
        isMissed -> MaterialTheme.colorScheme.error
        isIncoming -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val trailingIcon = if (isVideo) Icons.Filled.Videocam else Icons.Filled.Call
    
    val callDirectionIcon = when {
        isMissed -> Icons.Filled.CallMissed
        isIncoming -> Icons.Filled.CallReceived
        else -> Icons.Filled.CallMade
    }
    
    val nameColor = if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    
    val subText = when {
        isMissed && isVideo -> "Missed video call"
        isMissed -> "Missed audio call"
        isIncoming && isVideo -> "Incoming video call"
        isIncoming -> "Incoming audio call"
        isVideo -> "Outgoing video call"
        else -> "Outgoing audio call"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold, 
                    color = nameColor
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    callDirectionIcon, 
                    contentDescription = null, 
                    tint = iconColor, 
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$subText • $time",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Icon(
            trailingIcon, 
            contentDescription = "Action", 
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(36.dp)
                .padding(6.dp)
        )
    }
}
