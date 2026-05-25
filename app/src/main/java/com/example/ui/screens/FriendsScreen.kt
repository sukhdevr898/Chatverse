package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.*

@Composable
fun FriendsScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Friends",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = PureWhite),
                modifier = Modifier.padding(24.dp)
            )

            // Search / Add Friend Field
            AddFriendSearchField()

            Spacer(modifier = Modifier.height(24.dp))

            // Pending requests
            Text(
                "Friend Requests",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = PureWhite),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            FriendRequestList()
        }
    }
}

@Composable
fun AddFriendSearchField() {
    var query by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassCardBg)
            .border(1.dp, GlassInputBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = NeonBlue)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Add Friend by Username...", color = SoftGray)
        }
    }
}

data class FriendRequest(
    val id: String,
    val username: String,
    val mutuals: Int,
    val isVerified: Boolean = false
)

@Composable
fun FriendRequestList() {
    val requests = listOf(
        FriendRequest("1", "Kaelen", 5, true),
        FriendRequest("2", "Nova", 2, false)
    )

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(requests) { req ->
            FriendRequestCard(req)
        }
    }
}

@Composable
fun FriendRequestCard(req: FriendRequest) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassCardBg)
            .border(1.dp, GlassInputBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(StitchGradient3, ElectricPurple)))
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = req.username,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PureWhite)
                )
                if (req.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = NeonBlue, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${req.mutuals} mutual friends",
                style = MaterialTheme.typography.bodySmall.copy(color = SoftGray)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Accept
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .clickable {  },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Accept", tint = Color(0xFF10B981))
            }
            // Decline
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                    .clickable {  },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Decline", tint = Color(0xFFEF4444))
            }
        }
    }
}
