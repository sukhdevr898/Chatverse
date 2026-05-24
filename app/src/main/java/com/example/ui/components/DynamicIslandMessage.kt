package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.IslandMessage
import com.example.ui.MessageType
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.TextPrimary

@Composable
fun DynamicIslandMessage(messages: List<IslandMessage>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 24.dp, end = 24.dp), // Clear status bar
        contentAlignment = Alignment.TopCenter
    ) {
        messages.forEach { message ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn() + scaleIn(initialScale = 0.8f),
                exit = slideOutVertically(targetOffsetY = { -100 }) + fadeOut() + scaleOut(targetScale = 0.8f),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(DarkSurface.copy(alpha = 0.95f))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val iconColor = when (message.type) {
                            MessageType.SUCCESS -> Color(0xFF00FF88)
                            MessageType.ERROR -> ErrorRed
                            MessageType.INFO -> NeonBlue
                        }
                        val icon = when (message.type) {
                            MessageType.SUCCESS -> Icons.Filled.CheckCircle
                            MessageType.ERROR -> Icons.Filled.Error
                            MessageType.INFO -> Icons.Filled.Info
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = message.text,
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}
