package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AuthViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import com.example.data.toFriendRequest

import android.content.Intent
import com.example.services.MessagePollingService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.data.UserSession.init(this)
        
// Foreground service removed to prevent Android 14+ crash without proper permissions

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChatVerseApp()
            }
        }
    }
}


@Composable
fun ChatVerseApp(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        var lastSeenRequests = emptySet<String>()
        var isFirstPoll = true
        while(true) {
            val auth = com.example.data.UserSession.idToken
            val currentUserId = com.example.data.UserSession.userId
            if (auth != null && currentUserId != null) {
                try {
                    val projectId = com.example.data.FirestoreService.getProjectIdFromToken(auth)
                    val reqResponse = com.example.data.FirestoreService.api.getFriendRequests(projectId, currentUserId, "Bearer $auth")
                    if (!reqResponse.isSuccessful && reqResponse.code() == 401) {
                        com.example.data.UserSession.clear()
                        kotlinx.coroutines.Dispatchers.Main.dispatch(kotlin.coroutines.EmptyCoroutineContext, Runnable {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        })
                    } else if (reqResponse.isSuccessful) {
                        val currentRequests = reqResponse.body()?.documents?.mapNotNull { it.toFriendRequest() } ?: emptyList()
                        val currentSenderIds = currentRequests.map { it.senderId }.toSet()
                        
                        if (!isFirstPoll) {
                            val newSenderIds = currentSenderIds - lastSeenRequests
                            if (newSenderIds.isNotEmpty()) {
                                newSenderIds.forEach { senderId ->
                                    val req = currentRequests.find { it.senderId == senderId }
                                    if (req != null) {
                                        com.example.NotificationHelper.showSystemNotification(
                                            com.example.data.UserSession.appContext!!,
                                            "New Friend Request",
                                            "${req.senderUsername} sent you a friend request!"
                                        )
                                        authViewModel.showMessage("${req.senderUsername} sent you a friend request!", com.example.ui.MessageType.INFO)
                                    }
                                }
                            }
                        }
                        
                        lastSeenRequests = currentSenderIds
                        isFirstPoll = false
                    }
                } catch (e: Exception) {
                    // Ignore network errors in polling
                }
            } else {
                isFirstPoll = true
                lastSeenRequests = emptySet()
            }
            delay(10000)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PureBlack)) {
        // Shared animated background
        AnimatedGradientMeshBackground()

        NavHost(
            navController = navController,
            startDestination = "splash",
            enterTransition = { fadeIn(tween(500)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(500)) },
            exitTransition = { fadeOut(tween(500)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(500)) },
            popEnterTransition = { fadeIn(tween(500)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(500)) },
            popExitTransition = { fadeOut(tween(500)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(500)) }
        ) {
            composable("splash") { SplashScreen(navController) }
            composable("auth") { AuthScreen(navController, authViewModel) }
            composable("onboarding_name") { OnboardingNameScreen(navController, authViewModel) }
            composable("onboarding_dob") { OnboardingDobScreen(navController, authViewModel) }
            composable("onboarding_mobile") { OnboardingMobileScreen(navController, authViewModel) }
            composable("onboarding_bio") { OnboardingBioScreen(navController, authViewModel) }
            composable("main") { MainScreen(navController) }
            composable("chat/{chatId}/{username}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val username = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("username") ?: "", "UTF-8")
                ChatScreen(navController, chatId, username)
            }
        }

        // Overlay Dynamic Island at the top
        DynamicIslandMessage(messages = authViewModel.messages)
    }
}

// SplashScreen moved to ui.screens

// Simplified Auth UI moved to OnboardingScreen.kt

