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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CallsScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf("All recent") }
    var mockCallDialog by remember { mutableStateOf<CallLog?>(null) }
    var isCallActive by remember { mutableStateOf(false) }

    val allCalls = listOf(
        CallLog("Vikram Singh", "10:30 AM", CallType.MISSED_AUDIO),
        CallLog("Chhaya", "Yesterday", CallType.OUTGOING_VIDEO),
        CallLog("Java Goat Devs", "Sunday", CallType.INCOMING_AUDIO)
    )
    val missedCalls = allCalls.filter { it.type == CallType.MISSED_AUDIO || it.type == CallType.MISSED_VIDEO }

    if (mockCallDialog != null) {
        if (!isCallActive) {
            MockCallingOverlay(log = mockCallDialog!!, onCancel = { mockCallDialog = null }, onAccept = { isCallActive = true })
        } else {
            MockActiveCallOverlay(log = mockCallDialog!!, onEnd = {
                isCallActive = false
                mockCallDialog = null
            })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // App Header for Calls
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
                    text = "Calls",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color(0xFF111827)
                    )
                )
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFF3E8FF), CircleShape).clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AddIcCall, contentDescription = "Add", tint = Color(0xFFA855F7), modifier = Modifier.size(20.dp))
                }
            }

            // Tabs
            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("All recent", "Missed").forEach { tab ->
                    val isSelected = selectedTab == tab
                    val bg = if (isSelected) Color(0xFF111827) else Color(0xFFF3F4F6)
                    val textCol = if (isSelected) Color.White else Color(0xFF4B5563)
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedTab = tab }
                            .background(bg)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(tab, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textCol)
                    }
                }
            }

            // List
            val displayedCalls = if (selectedTab == "All recent") allCalls else missedCalls

            Box(modifier = Modifier.weight(1f).padding(horizontal = 20.dp)) {
                Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(displayedCalls.size) { index ->
                            CallHistoryItem(
                                log = displayedCalls[index],
                                onClick = { mockCallDialog = displayedCalls[index] }
                            )
                            if (index < displayedCalls.size - 1) {
                                HorizontalDivider(color = Color(0xFFF9FAFB), modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 24.dp)
                .size(64.dp)
                .background(Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF3B82F6))), CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Dialpad, contentDescription = "Dial", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

// ----------------------------------------------------
// UI Components
// ----------------------------------------------------

enum class CallType { INCOMING_AUDIO, OUTGOING_AUDIO, MISSED_AUDIO, INCOMING_VIDEO, OUTGOING_VIDEO, MISSED_VIDEO }
data class CallLog(val name: String, val time: String, val type: CallType)

@Composable
fun CallHistoryItem(log: CallLog, onClick: () -> Unit) {
    val isMissed = log.type == CallType.MISSED_AUDIO || log.type == CallType.MISSED_VIDEO
    val isVideo = log.type == CallType.INCOMING_VIDEO || log.type == CallType.OUTGOING_VIDEO || log.type == CallType.MISSED_VIDEO
    val isIncoming = log.type == CallType.INCOMING_AUDIO || log.type == CallType.INCOMING_VIDEO
    
    val iconColor = when {
        isMissed -> Color(0xFFEF4444)
        isIncoming -> Color(0xFF10B981)
        else -> Color.Gray
    }
    
    val trailingIcon = if (isVideo) Icons.Filled.Videocam else Icons.Filled.Call
    
    val callDirectionIcon = when {
        isMissed -> Icons.Filled.CallMissed
        isIncoming -> Icons.Filled.CallReceived
        else -> Icons.Filled.CallMade
    }
    
    val nameColor = if (isMissed) Color(0xFFEF4444) else Color(0xFF111827)
    
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
            .clickable(onClick = onClick)
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
            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.ExtraBold, 
                    color = nameColor,
                    fontSize = 16.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    callDirectionIcon, 
                    contentDescription = null, 
                    tint = iconColor, 
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = subText,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(log.time, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.size(32.dp).background(Color(0xFFF3E8FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    trailingIcon, 
                    contentDescription = "Action", 
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// Mock Call Overlays
// ----------------------------------------------------

@Composable
fun MockCallingOverlay(log: CallLog, onCancel: () -> Unit, onAccept: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6111827))
            .clickable(enabled = false) {}, // intercept clicks
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White, CircleShape)
                    .border(4.dp, Color(0xFFA855F7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(60.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(log.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Calling...", color = Color.Gray, fontSize = 16.sp)
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                // Cancel
                Box(
                    modifier = Modifier.size(64.dp).background(Color(0xFFEF4444), CircleShape).clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CallEnd, contentDescription = "End", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                
                // Accept (mocking that they picked up)
                Box(
                    modifier = Modifier.size(64.dp).background(Color(0xFF10B981), CircleShape).clickable(onClick = onAccept),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Call, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun MockActiveCallOverlay(log: CallLog, onEnd: () -> Unit) {
    var seconds by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while(true) {
            delay(1000)
            seconds++
        }
    }
    
    val timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
            .clickable(enabled = false) {}, // intercept clicks
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White, CircleShape)
                    .border(4.dp, Color(0xFF10B981), CircleShape), // Green border for active
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(60.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(log.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(timeStr, color = Color(0xFF10B981), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Box(modifier = Modifier.size(56.dp).background(Color.White.copy(alpha=0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = Color.White)
                }
                Box(modifier = Modifier.size(56.dp).background(Color.White.copy(alpha=0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.VideocamOff, contentDescription = null, tint = Color.White)
                }
                Box(modifier = Modifier.size(56.dp).background(Color.White.copy(alpha=0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.MicOff, contentDescription = null, tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Box(
                modifier = Modifier.size(72.dp).background(Color(0xFFEF4444), CircleShape).clickable(onClick = onEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CallEnd, contentDescription = "End", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}