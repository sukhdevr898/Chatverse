package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.launch
import com.example.ui.AuthViewModel
import com.example.ui.MessageType
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        if (com.example.data.UserSession.idToken != null) {
            navController.navigate("main") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("auth") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "logo_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale).alpha(alpha)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFE81CFF), Color(0xFF41B5FF))),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ChatBubble, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(60.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "ChatVerse",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    brush = Brush.linearGradient(listOf(Color(0xFFE81CFF), Color(0xFF41B5FF)))
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rista dil se dil tak",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = Color.Gray,
                modifier = Modifier.alpha(0.7f)
            )
        }
    }
}

@Composable
fun AuthScreen(navController: NavController, authViewModel: AuthViewModel) {
    val isLoading by authViewModel.isLoading.collectAsState(initial = false)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Decorative Blobs
        Box(modifier = Modifier.offset(x = (-80).dp, y = (-80).dp).size(280.dp).blur(60.dp).background(Color(0xFFE9D5FF), CircleShape).alpha(0.4f))
        Box(modifier = Modifier.align(Alignment.CenterEnd).offset(x = 80.dp, y = 100.dp).size(320.dp).blur(60.dp).background(Color(0xFFBFDBFE), CircleShape).alpha(0.4f))

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFFE81CFF), Color(0xFF41B5FF))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ChatBubble, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(48.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Welcome to\nChatVerse",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
                color = Color(0xFF111827),
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Connect securely and seamlessly.\nSign in to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isEmpty()) {
                        Toast.makeText(context, "Please configure GOOGLE_WEB_CLIENT_ID in the Secrets panel", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    
                    val credentialManager = CredentialManager.create(context)
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .setAutoSelectEnabled(true)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    coroutineScope.launch {
                        try {
                            val result = credentialManager.getCredential(context = context, request = request)
                            val credential = result.credential
                            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                // We simulate passing the token to backend
                                val idToken = googleIdTokenCredential.idToken
                                authViewModel.login(googleIdTokenCredential.id, "google_auth_123456") {
                                    navController.navigate("onboarding_name")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Auth", "Google Login failed", e)
                            Toast.makeText(context, "Google Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFA855F7), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Continue with Google", color = Color(0xFF111827), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "By continuing, you agree to our Terms and Privacy Policy.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OnboardingHeader(step: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color.White, CircleShape).clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Gray)
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..4) {
                val width = if (i == step) 24.dp else 8.dp
                val color = if (i == step) Color(0xFFA855F7) else Color(0xFFE5E7EB)
                Box(modifier = Modifier.height(6.dp).width(width).background(color, CircleShape))
            }
        }
    }
}

@Composable
fun OnboardingNameScreen(navController: NavController) {
    var name by remember { mutableStateOf("Aryan Kapoor") }
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        OnboardingHeader(step = 1) { navController.popBackStack() }
        
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("What's your name?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)))
            Spacer(modifier = Modifier.height(8.dp))
            Text("This will be your display name on ChatVerse.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { navController.navigate("onboarding_dob") },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun OnboardingDobScreen(navController: NavController) {
    var dob by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        OnboardingHeader(step = 2) { navController.popBackStack() }
        
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("When is your birthday?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)))
            Spacer(modifier = Modifier.height(8.dp))
            Text("You must be at least 13 years old to use ChatVerse.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("DD/MM/YYYY") },
                leadingIcon = { Icon(Icons.Filled.EditCalendar, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { navController.navigate("onboarding_mobile") },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun OnboardingMobileScreen(navController: NavController) {
    var mobile by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        OnboardingHeader(step = 3) { navController.popBackStack() }
        
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Add mobile number", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Used to help friends find you.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("9876543210") },
                leadingIcon = { Text("+91", modifier = Modifier.padding(start=16.dp, end=8.dp), fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Privacy selector mock
            Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).background(Color(0xFFEFF6FF), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Number Privacy", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Who can see this number on your profile?", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("My Contacts Only", fontWeight = FontWeight.SemiBold, color = Color(0xFF111827), modifier = Modifier.background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp)).padding(8.dp).fillMaxWidth())
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { navController.navigate("onboarding_bio") },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun OnboardingBioScreen(navController: NavController, authViewModel: AuthViewModel) {
    var bio by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        OnboardingHeader(step = 4) { navController.popBackStack() }
        
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Write a short bio", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tell your friends a little about yourself.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Living life, one cup of coffee at a time ☕") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                ),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    authViewModel.showMessage("Profile Setup Complete!", MessageType.SUCCESS)
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFE81CFF), Color(0xFF41B5FF))), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Finish Setup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}
