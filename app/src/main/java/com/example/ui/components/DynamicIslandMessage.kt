package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.IslandMessage
import com.example.ui.MessageType

@Composable
fun DynamicIslandMessage(messages: List<IslandMessage>) {
    val currentMessage = messages.lastOrNull()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 24.dp, end = 24.dp), // Clear status bar
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = currentMessage != null,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300), initialOffsetY = { -100 }),
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300), targetOffsetY = { -100 }),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            currentMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .defaultMinSize(minWidth = 140.dp)
                        .height(48.dp)
                        .shadow(elevation = 24.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x66000000), ambientColor = Color(0x33000000))
                        .background(Color.Black, shape = RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = Pair(message.type, message.text),
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "IslandContent",
                        modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                    ) { (type, text) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            if (type == MessageType.LOADING) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = text,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            } else {
                                if (type == MessageType.SUCCESS) {
                                    Box(modifier = Modifier.size(24.dp).background(Color(0xFF22C55E), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                } else if (type == MessageType.ERROR) {
                                    Box(modifier = Modifier.size(24.dp).background(Color(0xFFEF4444), CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(10.dp))

                                Column(horizontalAlignment = Alignment.Start) {
                                    if (text.contains("|")) {
                                        Text(
                                            text = text.substringBefore("|"), 
                                            color = if (type == MessageType.SUCCESS) Color(0xFF4ADE80) else Color(0xFFF87171),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = text.substringAfter("|"),
                                            color = Color(0xFFE5E7EB),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                    } else {
                                        Text(
                                            text = text,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 1
                                        )
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
