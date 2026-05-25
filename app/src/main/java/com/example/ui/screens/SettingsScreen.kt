package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.*

@Composable
fun SettingsScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp, top = 24.dp)
        ) {
            item {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = PureWhite),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
            
            item {
                SettingsProfileCard()
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            item {
                SettingsCategory("Account")
                SettingsMenuItem(Icons.Filled.Person, "Account Settings", "Password, Security")
                SettingsMenuItem(Icons.Filled.Lock, "Privacy", "Who can message me")
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item {
                SettingsCategory("Preferences")
                SettingsMenuItem(Icons.Filled.Notifications, "Notifications", "Sounds & Vibes")
                SettingsMenuItem(Icons.Filled.Palette, "Theme", "Dark AMOLED, Cyber Neon")
                SettingsMenuItem(Icons.Filled.Language, "Language", "English (US)")
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item {
                SettingsCategory("Danger Zone")
                SettingsMenuItem(Icons.Filled.Block, "Block List", "Manage blocked contacts", isDanger = true)
                SettingsMenuItem(Icons.AutoMirrored.Filled.Logout, "Log Out", "", isDanger = true, onClick = {
                    navController.navigate("login") { popUpTo(0) }
                })
            }
        }
    }
}

@Composable
fun SettingsProfileCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassCardBg)
            .border(1.dp, GlassInputBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Graphite)
                    .border(2.dp, NeonBlue, CircleShape)
            ) {
                // Placeholder avatar inside
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(12.dp), tint = SoftGray)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Alex Denton",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = PureWhite)
                )
                Text(
                    "@alex_cyber",
                    style = MaterialTheme.typography.bodyMedium.copy(color = NeonBlue)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.1f))
                    .clickable {  },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = PureWhite, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = NeonBlue),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDanger: Boolean = false,
    onClick: () -> Unit = {}
) {
    val color = if (isDanger) Color(0xFFEF4444) else PureWhite
    val iconColor = if (isDanger) Color(0xFFEF4444) else SoftGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(GlassCardBg)
                .border(1.dp, GlassInputBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = color))
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = SoftGray))
            }
        }
        
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = SoftGray)
    }
}
