package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Preview Reload Trigger
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
            composable("login") { LoginScreen(navController, authViewModel) }
            composable("signup") { SignupScreen(navController, authViewModel) }
            composable("forgotPassword") { ForgotPasswordScreen(navController, authViewModel) }
        }

        // Overlay Dynamic Island at the top
        DynamicIslandMessage(messages = authViewModel.messages)
    }
}

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "logo_alpha"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale).alpha(alpha)
        ) {
            Icon(
                imageVector = Icons.Filled.ChatBubble,
                contentDescription = "Logo",
                tint = NeonBlue,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "ChatVerse",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.horizontalGradient(listOf(NeonBlue, ElectricPurple))
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rista dil se dil tk",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Light,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
            )
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val isLoading by authViewModel.isLoading.collectAsState(initial = false)

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { 200 }, animationSpec = tween(800))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.ChatBubble,
                    contentDescription = "Logo",
                    tint = NeonBlue,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connect heart to heart instantly",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(40.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PremiumTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            leadingIcon = { Icon(Icons.Outlined.Email, "Email", tint = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PremiumTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            leadingIcon = { Icon(Icons.Outlined.Lock, "Password", tint = TextSecondary) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = NeonBlue,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.clickable { navController.navigate("forgotPassword") }
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        GlowButton(
                            text = "LOGIN",
                            onClick = {
                                authViewModel.login(email, password) {
                                    // Normally navigate to home
                                }
                            },
                            isLoading = isLoading
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text("Or continue with", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialButton(icon = { Icon(Icons.Filled.AccountCircle, "Google", tint = TextPrimary, modifier = Modifier.size(28.dp)) }) {}
                    SocialButton(icon = { Icon(Icons.Filled.Face, "Facebook", tint = TextPrimary, modifier = Modifier.size(28.dp)) }) {}
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Don't have an account? ", color = TextSecondary)
                    Text(
                        "Create Account",
                        color = ElectricPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navController.navigate("signup") }
                    )
                }
            }
        }
    }
}

@Composable
fun SignupScreen(navController: NavController, authViewModel: AuthViewModel) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val isLoading by authViewModel.isLoading.collectAsState(initial = false)

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { 200 }, animationSpec = tween(800))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Your Universe",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start your journey on ChatVerse",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(40.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PremiumTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = "Username",
                            leadingIcon = { Icon(Icons.Outlined.Person, "Username", tint = TextSecondary) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PremiumTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            leadingIcon = { Icon(Icons.Outlined.Email, "Email", tint = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PremiumTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            leadingIcon = { Icon(Icons.Outlined.Lock, "Password", tint = TextSecondary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        GlowButton(
                            text = "SIGN UP",
                            onClick = {
                                authViewModel.signup(email, password) {
                                    navController.popBackStack()
                                }
                            },
                            isLoading = isLoading
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Already have an account? ", color = TextSecondary)
                    Text(
                        "Login",
                        color = NeonBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    
    val isLoading by authViewModel.isLoading.collectAsState(initial = false)

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(800))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Lock",
                    tint = CyanAccent,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if(isSubmitted) "Check your email" else "Reset Password",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if(isSubmitted) "We sent a recovery link to you." else "We'll help you recover your account",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(40.dp))

                if (!isSubmitted) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PremiumTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                leadingIcon = { Icon(Icons.Outlined.Email, "Email", tint = TextSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            GlowButton(
                                text = "SEND RESET LINK",
                                onClick = {
                                    authViewModel.resetPassword(email) {
                                        isSubmitted = true
                                    }
                                },
                                isLoading = isLoading
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    "Back to Login",
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }
        }
    }
}

