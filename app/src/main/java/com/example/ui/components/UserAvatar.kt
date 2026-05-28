package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Face2
import androidx.compose.material.icons.filled.Face3
import androidx.compose.material.icons.filled.Face4
import androidx.compose.material.icons.filled.Face5
import androidx.compose.material.icons.filled.Face6
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun UserAvatar(avatarId: Int, modifier: Modifier = Modifier) {
    val (icon, color) = when (avatarId) {
        1 -> Icons.Filled.Face to Color(0xFFFFCC80)
        2 -> Icons.Filled.Face2 to Color(0xFFC5E1A5)
        3 -> Icons.Filled.Face3 to Color(0xFF90CAF9)
        4 -> Icons.Filled.Face4 to Color(0xFFF48FB1)
        5 -> Icons.Filled.Face5 to Color(0xFFCE93D8)
        6 -> Icons.Filled.Face6 to Color(0xFFFFAB91)
        else -> Icons.Filled.Person to Color(0xFFE0E0E0)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Avatar",
            tint = Color.White,
            modifier = Modifier.fillMaxSize().padding(12.dp)
        )
    }
}
