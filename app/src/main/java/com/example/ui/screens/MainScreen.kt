package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.*

@Composable
fun MainScreen(rootNavController: NavController, authViewModel: com.example.ui.AuthViewModel) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { PremiumBottomNavBar(bottomNavController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = bottomNavController,
                startDestination = "chats",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }
            ) {
                composable("chats") { HomeScreen(rootNavController, onNavigateToFriends = {
                    bottomNavController.navigate("friends") {
                        popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }, authViewModel = authViewModel) }
                composable("friends") { FriendsScreen(rootNavController, viewModel = androidx.lifecycle.viewmodel.compose.viewModel()) }
                composable("calls") { CallsScreen(rootNavController) }
                composable("settings") { SettingsScreen(rootNavController) }
            }
        }
    }
}

@Composable
fun PremiumBottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val items = listOf(
        NavItem("chats", "Chats", Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble),
        NavItem("friends", "Friends", Icons.Outlined.PeopleOutline, Icons.Filled.People),
        NavItem("calls", "Calls", Icons.Outlined.Call, Icons.Filled.Call),
        NavItem("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                NavBarItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val iconColor = if (isSelected) Color(0xFFA855F7) else Color(0xFF9CA3AF)
    val textColor = if (isSelected) Color(0xFFA855F7) else Color(0xFF9CA3AF)
    
    Column(
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.activeIcon, // Using filled icons by default in this design style
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold, 
                color = textColor,
                fontSize = 10.sp
            )
        )
    }
}

data class NavItem(val route: String, val label: String, val icon: ImageVector, val activeIcon: ImageVector)
