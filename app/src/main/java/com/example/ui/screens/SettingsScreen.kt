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
            .background(MaterialTheme.colorScheme.background)
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp, top = 24.dp)
        ) {
            item {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
            
            item {
                SettingsProfileCard()
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            item {
                SettingsCategory("Account")
                SettingsMenuItem(Icons.Filled.Person, "Account Settings", "Password, Security") {
                    android.widget.Toast.makeText(context, "Account Settings not yet implemented", android.widget.Toast.LENGTH_SHORT).show()
                }
                SettingsMenuItem(Icons.Filled.Lock, "Privacy", "Who can message me") {
                    android.widget.Toast.makeText(context, "Privacy settings not yet implemented", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item {
                SettingsCategory("Preferences")
                SettingsMenuItem(Icons.Filled.Notifications, "Notifications", "Sounds & Vibes") {
                    android.widget.Toast.makeText(context, "Notifications not yet implemented", android.widget.Toast.LENGTH_SHORT).show()
                }
                SettingsMenuItem(Icons.Filled.Palette, "Theme", "Dark AMOLED, Cyber Neon") {
                    android.widget.Toast.makeText(context, "Themes not yet implemented", android.widget.Toast.LENGTH_SHORT).show()
                }
                SettingsMenuItem(Icons.Filled.Language, "Language", "English (US)") {
                    android.widget.Toast.makeText(context, "Language selection not yet implemented", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item {
                SettingsCategory("Danger Zone")
                SettingsMenuItem(Icons.Filled.Block, "Block List", "Manage blocked contacts", isDanger = true) {
                    android.widget.Toast.makeText(context, "Block list not yet implemented", android.widget.Toast.LENGTH_SHORT).show()
                }
                SettingsMenuItem(Icons.AutoMirrored.Filled.Logout, "Log Out", "", isDanger = true, onClick = {
                    com.example.data.UserSession.clear()
                    navController.navigate("onboarding") { popUpTo(0) }
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha=0.05f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                // Placeholder avatar inside
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                val email = com.example.data.UserSession.email ?: "Unknown User"
                val username = email.substringBefore("@")
                Text(
                    username,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    "@$username",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
                    .clickable {  },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
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
    val color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
    val iconColor = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

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
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha=0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = color))
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
        }
        
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
