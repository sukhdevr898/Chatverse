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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.data.UserSession.init(this)
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
            composable("onboarding") { OnboardingScreen(navController) }
            composable("login") { LoginScreen(navController, authViewModel) }
            composable("signup") { SignupScreen(navController, authViewModel) }
            composable("forgotPassword") { ForgotPasswordScreen(navController, authViewModel) }
            composable("main") { MainScreen(navController) }
            composable("chat/{chatId}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                ChatScreen(navController, chatId)
            }
        }

        // Overlay Dynamic Island at the top
        DynamicIslandMessage(messages = authViewModel.messages)
    }
}

// SplashScreen moved to ui.screens

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
                // Header matching HTML
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(StitchPrimaryContainer.copy(alpha = 0.2f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChatBubble,
                        contentDescription = "Logo",
                        tint = StitchPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "ChatVerse",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.bodyLarge,
                    color = StitchSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(40.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        PremiumTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email Address",
                            leadingIcon = { Icon(Icons.Outlined.Email, "Email", tint = StitchSurfaceVariant) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PremiumTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            leadingIcon = { Icon(Icons.Outlined.Lock, "Password", tint = StitchSurfaceVariant) },
                            trailingIcon = {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = StitchSurfaceVariant,
                                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                                )
                            },
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
                                color = StitchPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.clickable { navController.navigate("forgotPassword") }
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        GlowButton(
                            text = "Login",
                            onClick = {
                                authViewModel.login(email, password) {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            },
                            isLoading = isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SocialButton(
                                text = "Google",
                                icon = { 
                                    // Simplified Google icon approach since we don't have SVG inline
                                    Text("G", fontWeight = FontWeight.ExtraBold, color = Color(0xFF4285F4))
                                },
                                onClick = {},
                                modifier = Modifier.weight(1f)
                            )
                            SocialButton(
                                text = "Apple",
                                icon = {
                                    Icon(Icons.Filled.PhoneIphone, "Apple", tint = TextPrimary)
                                },
                                onClick = {},
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Don't have an account? ", color = StitchSurfaceVariant)
                    Text(
                        "Create Account",
                        color = StitchPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp).clickable { navController.navigate("signup") }
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SignupScreen(navController: NavController, authViewModel: AuthViewModel) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val isLoading by authViewModel.isLoading.collectAsState(initial = false)

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // Password strength logic
    val strength = when {
        password.isEmpty() -> 0
        password.length < 6 -> 1 // weak
        password.length < 10 -> 2 // medium
        else -> 3 // strong
    }

    Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp, bottom = 16.dp), contentAlignment = Alignment.Center) {
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
                // Header matching HTML
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BubbleChart,
                        contentDescription = "Logo",
                        tint = StitchPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "ChatVerse",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = StitchPrimary
                    )
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Picker
                        Box(
                            modifier = Modifier
                                .offset(y = (-56).dp)
                                .size(96.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(StitchBackground.copy(alpha = 0.5f))
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(listOf(StitchGradient3.copy(0.3f), StitchGradient3.copy(0.1f))),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(StitchGradient3.copy(alpha = 0.15f))
                                    .border(1.dp, StitchGradient3.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PhotoCamera, "Avatar", tint = StitchGradient3, modifier = Modifier.size(20.dp))
                            }
                        }

                        Column(modifier = Modifier.offset(y = (-32).dp)) {
                            PremiumTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = "Username",
                                leadingIcon = null
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            PremiumTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email Address",
                                leadingIcon = null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            PremiumTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Password",
                                leadingIcon = null,
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = StitchSurfaceVariant,
                                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                                    )
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )
                            
                            // Strength Meter
                            if (password.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val color1 = if (strength >= 1) {
                                        if (strength == 1) ErrorRed else if (strength == 2) StitchPrimary else StitchGradient3
                                    } else Color.White.copy(0.1f)
                                    
                                    val color2 = if (strength >= 2) {
                                        if (strength == 2) StitchPrimary else StitchGradient3
                                    } else Color.White.copy(0.1f)
                                    
                                    val color3 = if (strength >= 3) StitchGradient3 else Color.White.copy(0.1f)
                                    
                                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color1))
                                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color2))
                                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color3))
                                    
                                    val labelText = when(strength) {
                                        1 -> "Weak"
                                        2 -> "Medium"
                                        3 -> "Strong"
                                        else -> ""
                                    }
                                    val labelColor = when(strength) {
                                        1 -> ErrorRed
                                        2 -> StitchPrimary
                                        3 -> StitchGradient3
                                        else -> Color.Transparent
                                    }
                                    
                                    Text(
                                        text = labelText,
                                        color = labelColor,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            PremiumTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "Confirm Password",
                                leadingIcon = null,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                                errorMessage = if (confirmPassword.isNotEmpty() && confirmPassword != password) "Passwords do not match" else null
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            
                            GlowButton(
                                text = "Create Account",
                                onClick = {
                                    if (password == confirmPassword) {
                                        authViewModel.signup(email, password) {
                                            navController.popBackStack()
                                        }
                                    }
                                },
                                isLoading = isLoading
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(0.1f))
                                Text("OR", color = StitchSurfaceVariant, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 16.dp))
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(0.1f))
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            SocialButton(
                                text = "Continue with Google",
                                icon = { 
                                    Text("G", fontWeight = FontWeight.ExtraBold, color = Color(0xFF4285F4))
                                },
                                onClick = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Already have an account? ", color = StitchSurfaceVariant)
                    Text(
                        "Login",
                        color = StitchGradient3,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp).clickable { navController.popBackStack() }
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
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
            enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { 200 }, animationSpec = tween(800))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BubbleChart,
                        contentDescription = "Logo",
                        tint = StitchPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "ChatVerse",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = StitchPrimary
                    )
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!isSubmitted) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(StitchPrimaryContainer.copy(alpha = 0.1f))
                                    .border(1.dp, StitchPrimary.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Lock",
                                    tint = StitchGradient3,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Forgot Password?",
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enter your email to reset",
                                style = MaterialTheme.typography.bodyLarge,
                                color = StitchSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            PremiumTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email Address",
                                leadingIcon = { Icon(Icons.Outlined.Email, "Email", tint = StitchSurfaceVariant) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            GlowButton(
                                text = "Send Reset Link",
                                onClick = {
                                    authViewModel.resetPassword(email) {
                                        isSubmitted = true
                                    }
                                },
                                isLoading = isLoading
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = StitchSurfaceVariant, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Back to Login",
                                    color = StitchSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(StitchGradient3.copy(alpha = 0.1f))
                                    .border(1.dp, StitchGradient3.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Success",
                                    tint = StitchGradient3,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Email Sent!",
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "We've sent a password reset link to your email address. Please check your inbox.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = StitchSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            GlowButton(
                                text = "Return to Login",
                                onClick = { navController.popBackStack() }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            SocialButton(
                                text = "Resend Link",
                                icon = { 
                                    Icon(Icons.Filled.Refresh, "Resend", tint = TextPrimary)
                                },
                                onClick = {
                                    authViewModel.resetPassword(email) {}
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

