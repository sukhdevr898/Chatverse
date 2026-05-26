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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.*

@Composable
fun MainScreen(rootNavController: NavController) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        containerColor = DeepBlack,
        bottomBar = { PremiumBottomNavBar(bottomNavController) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                }) }
                composable("friends") { FriendsScreen(rootNavController) }
                composable("calls") { CallsScreen(rootNavController) }
                composable("stories") { StoriesScreen(rootNavController) }
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
        NavItem("stories", "Stories", Icons.Outlined.Panorama, Icons.Filled.Panorama),
        NavItem("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(GlassCardBg)
                .padding(horizontal = 8.dp),
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
    
    // Animation for width expansion
    val backgroundAlpha by animateFloatAsState(if (isSelected) 0.15f else 0f)
    val iconColor = if (isSelected) NeonBlue else SoftGray
    
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .background(if (isSelected) NeonBlue.copy(alpha = backgroundAlpha) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isSelected) item.activeIcon else item.icon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.size(26.dp)
        )
    }
}

data class NavItem(val route: String, val label: String, val icon: ImageVector, val activeIcon: ImageVector)
