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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*

@Composable
fun SettingsScreen(navController: NavController, homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val currentUser by homeViewModel.currentUser.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF3F4F6))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(32.dp).background(Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF3B82F6))), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827), fontSize = 24.sp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
            ) {
                item {
                    val email = com.example.data.UserSession.email ?: "Unknown User"
                    val username = email.substringBefore("@")
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.example.ui.components.UserAvatar(
                            avatarId = currentUser?.avatarId ?: 0,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(currentUser?.name.takeIf { !it.isNullOrBlank() } ?: username, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF111827))
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                            }
                            Text(currentUser?.username ?: "@$username", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFFF3E8FF), CircleShape).clickable {},
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    SettingsSectionTitle("Account & Security")
                    Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))) {
                        Column {
                            SettingsOption(icon = Icons.Filled.PrivacyTip, iconBg = Color(0xFFEFF6FF), iconColor = Color(0xFF3B82F6), title = "Privacy & Security")
                            HorizontalDivider(color = Color(0xFFF9FAFB))
                            SettingsOption(icon = Icons.Filled.Block, iconBg = Color(0xFFFEF2F2), iconColor = Color(0xFFEF4444), title = "Blocklist", badge = "50+")
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    SettingsSectionTitle("Preferences")
                    Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))) {
                        Column {
                            SettingsOption(icon = Icons.Filled.Palette, iconBg = Color(0xFFF3E8FF), iconColor = Color(0xFFA855F7), title = "Theme Appearance", trailingText = "System")
                            HorizontalDivider(color = Color(0xFFF9FAFB))
                            SettingsOption(icon = Icons.Filled.Storage, iconBg = Color(0xFFF0FDF4), iconColor = Color(0xFF22C55E), title = "Storage & Data")
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    SettingsSectionTitle("Notification Simulator", icon = Icons.Filled.AutoFixHigh, iconColor = Color(0xFFA855F7))
                    Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, Color(0xFFF3E8FF), RoundedCornerShape(24.dp))) {
                        Column {
                            SettingsOption(icon = Icons.Filled.Chat, iconBg = Color.Transparent, iconColor = Color(0xFFA855F7), title = "Test Message Alert", trailingIcon = Icons.Filled.PlayArrow)
                            HorizontalDivider(color = Color(0xFFF9FAFB))
                            SettingsOption(icon = Icons.Filled.Phone, iconBg = Color.Transparent, iconColor = Color(0xFF22C55E), title = "Test Incoming Call", trailingIcon = Icons.Filled.PlayArrow)
                            HorizontalDivider(color = Color(0xFFF9FAFB))
                            SettingsOption(icon = Icons.Filled.PersonAdd, iconBg = Color.Transparent, iconColor = Color(0xFF3B82F6), title = "Test Friend Request", trailingIcon = Icons.Filled.PlayArrow)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Button(
                        onClick = {
                            com.example.data.UserSession.clear()
                            navController.navigate("auth") { popUpTo(0) }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("ChatVerse v2.2.0", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String, icon: ImageVector? = null, iconColor: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
        if (icon != null && iconColor != null) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = iconColor ?: Color.Gray, letterSpacing = 1.sp)
    }
}

@Composable
fun SettingsOption(icon: ImageVector, iconBg: Color, iconColor: Color, title: String, badge: String? = null, trailingText: String? = null, trailingIcon: ImageVector = Icons.Filled.ChevronRight) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(iconBg, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF374151))
        
        if (badge != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.background(Color(0xFFEF4444), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (trailingText != null) {
            Text(trailingText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(trailingIcon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}
