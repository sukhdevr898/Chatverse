package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.AuthViewModel
import com.example.ui.MessageType
import kotlinx.coroutines.delay

@Composable
fun ChatVerseLogo(modifier: Modifier = Modifier) {
    val gradientColors = listOf(Color(0xFFE13BFF), Color(0xFF8E44FF), Color(0xFF2FA1FF))
    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(10f, 90f),
        end = Offset(90f, 10f)
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h) / 100f
        
        val mainPath = Path().apply {
            addOval(Rect(15f*s, 15f*s, 85f*s, 85f*s))
            val tailPath = Path().apply {
                moveTo(23f*s, 75f*s)
                lineTo(12f*s, 94f*s)
                lineTo(35f*s, 83f*s)
                close()
            }
            addPath(tailPath)
        }
        
        val cutOutPath = Path().apply {
            addOval(Rect(27f*s, 27f*s, 73f*s, 73f*s))
            addRect(Rect(50f*s, 27f*s, 100f*s, 73f*s))
        }
        
        val savePath = Path().apply {
            addOval(Rect(38.5f*s, 15.5f*s, 61.5f*s, 38.5f*s))
            addOval(Rect(38.5f*s, 61.5f*s, 61.5f*s, 84.5f*s))
        }
        
        val finalCutOut = Path().apply {
            op(cutOutPath, savePath, PathOperation.Difference)
        }
        
        val finalShape = Path().apply {
            op(mainPath, finalCutOut, PathOperation.Difference)
        }

        drawPath(finalShape, brush)
        
        drawCircle(
            brush = brush,
            radius = 10f * s,
            center = Offset(73f * s, 50f * s)
        )
    }
}

@Composable
fun DynamicIslandNotification(authViewModel: AuthViewModel) {
    val messages = authViewModel.messages
    val currentMessage = messages.lastOrNull()
    val messageText = currentMessage?.text ?: ""
    val messageType = currentMessage?.type ?: MessageType.INFO

    DynamicIslandLocal(messageText, messageType)
}

@Composable
fun DynamicIslandLocal(messageText: String, messageType: MessageType) {
    val isVisible = messageText.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { -it }, animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500)) + slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(500)),
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black, RoundedCornerShape(32.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (messageType == MessageType.LOADING) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Text(
                        text = messageText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                } else {
                    if (messageType == MessageType.SUCCESS) {
                        Box(modifier = Modifier.size(36.dp).background(Color(0xFF22C55E), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    } else if (messageType == MessageType.ERROR) {
                        Box(modifier = Modifier.size(36.dp).background(Color(0xFFEF4444), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    Column {
                        Text(
                            text = if (messageText.contains("|")) messageText.substringBefore("|") else if (messageType == MessageType.SUCCESS) "Success" else "Error", 
                            color = if (messageType == MessageType.SUCCESS) Color(0xFF4ADE80) else Color(0xFFF87171),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (messageText.contains("|")) messageText.substringAfter("|") else messageText,
                            color = Color(0xFFE5E7EB),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

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

    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
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
            modifier = Modifier.alpha(alpha)
        ) {
            ChatVerseLogo(modifier = Modifier.size(128.dp).scale(pulse))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "ChatVerse",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                ),
                color = Color.Transparent,
                modifier = Modifier.background(
                    Brush.linearGradient(listOf(Color(0xFFE81CFF), Color(0xFF41B5FF))), // Equivalent to text-transparent bg-clip-text
                    alpha = 0.99f // Needed for compose text gradient blending trick sometimes but not always
                )
            )
            // Properly implementing text gradient
            Text(
                text = "ChatVerse",
                style = androidx.compose.ui.text.TextStyle(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    brush = Brush.linearGradient(listOf(Color(0xFFE81CFF), Color(0xFF41B5FF)))
                ),
                modifier = Modifier.offset(y = (-40).dp) // Laying over the transparent block or just remove the previous Text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "RISTA DIL SE DIL TAK",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier.offset(y = (-40).dp)
            )
        }
    }
}

@Composable
fun AuthScreen(navController: NavController, authViewModel: AuthViewModel) {
    val isLoading by authViewModel.isLoading.collectAsState(initial = false)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition()
    val purpleBlobY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "purpleBlobY"
    )
    val blueBlobX by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blueBlobX"
    )
    
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Decorative Blobs
        Box(modifier = Modifier.offset(x = 0.dp, y = purpleBlobY.dp - 60.dp).size(280.dp).background(Color(0xFFE9D5FF), CircleShape).alpha(0.4f))
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = blueBlobX.dp, y = 0.dp).size(320.dp).background(Color(0xFFBFDBFE), CircleShape).alpha(0.4f))

        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Logo
            Box(contentAlignment = Alignment.Center) {
                // If you have a chatverse_logo.png in drawable, you can use Image(painterResource(id = R.drawable.chatverse_logo), ...)
                // Using the requested placeholder compose icon based on chatverse_logo
                ChatVerseLogo(modifier = Modifier.size(110.dp))
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            Text(
                text = "ChatVerse",
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = Color(0xFF111111)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "rista dil se dil tak",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF777777),
                fontSize = 13.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isEmpty() || BuildConfig.GOOGLE_WEB_CLIENT_ID == "MY_GOOGLE_WEB_CLIENT_ID") {
                        authViewModel.showMessage("Please configure GOOGLE_WEB_CLIENT_ID", MessageType.ERROR)
                        return@Button
                    }
                    
                    authViewModel.showMessage("Verifying Account...", MessageType.LOADING)

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
                                val baseId = googleIdTokenCredential.id.replace(" ", "")
                                val syntheticEmail = if (baseId.contains("@")) baseId else "$baseId@gmail.com"
                                
                                authViewModel.showMessage("Welcome to ChatVerse", MessageType.SUCCESS)
                                delay(1000)
                                authViewModel.login(syntheticEmail, "google_auth_123456") {
                                    navController.navigate("onboarding_name")
                                }
                                
                                coroutineScope.launch {
                                    delay(2000)
                                    val currentMessage = authViewModel.messages.lastOrNull()?.text ?: ""
                                    if (currentMessage.contains("not registered") || currentMessage.contains("Failed") || currentMessage.contains("Invalid")) {
                                        authViewModel.signup(syntheticEmail, "google_auth_123456") {
                                            authViewModel.showMessage("Google Account Linked|Complete your profile", MessageType.SUCCESS)
                                            navController.navigate("onboarding_name")
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Auth", "Google Login failed", e)
                            authViewModel.showMessage("Google Login failed", MessageType.ERROR)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(bottom = 20.dp).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF111111), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = "Google", tint = Color.Unspecified, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (isLoading) "Please wait..." else "Continue with Google", color = Color(0xFF111111), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
            
            Text(
                text = "By continuing, you agree to Terms & Privacy Policy",
                color = Color(0xFF999999),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            DynamicIslandNotification(authViewModel)
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
                val color = if (i == step) Color(0xFFA855F7) else if (i < step) Color(0xFFE9D5FF) else Color(0xFFE5E7EB)
                Box(modifier = Modifier.height(6.dp).width(width).background(color, CircleShape))
            }
        }
    }
}

@Composable
fun OnboardingNameScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }
    
    LaunchedEffect(localError) {
        if (localError.isNotEmpty()) {
            delay(2500)
            localError = ""
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
            OnboardingHeader(step = 1) { navController.popBackStack() }
            
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("What's your name?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)))
                Spacer(modifier = Modifier.height(8.dp))
                Text("This will be your display name on ChatVerse.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Full Name", color = Color(0xFF9CA3AF)) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF9CA3AF)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 14.sp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            localError = "Please enter your name"
                        } else {
                            navController.navigate("onboarding_dob")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
        
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            DynamicIslandLocal(localError, MessageType.ERROR)
        }
    }
}

@Composable
fun OnboardingDobScreen(navController: NavController) {
    var dob by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }
    
    LaunchedEffect(localError) {
        if (localError.isNotEmpty()) {
            delay(2500)
            localError = ""
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
            OnboardingHeader(step = 2) { navController.popBackStack() }
            
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("When is your birthday?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)))
                Spacer(modifier = Modifier.height(8.dp))
                Text("You must be at least 13 years old to use ChatVerse.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("DD/MM/YYYY", color = Color(0xFF9CA3AF)) },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color(0xFF9CA3AF)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 14.sp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        if (dob.isBlank() || dob.length < 8) {
                            localError = "Please enter a valid date"
                        } else {
                            navController.navigate("onboarding_mobile")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
        
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            DynamicIslandLocal(localError, MessageType.ERROR)
        }
    }
}

@Composable
fun OnboardingMobileScreen(navController: NavController) {
    var mobile by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }
    
    LaunchedEffect(localError) {
        if (localError.isNotEmpty()) {
            delay(2500)
            localError = ""
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
            OnboardingHeader(step = 3) { navController.popBackStack() }
            
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Add mobile number", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Used to help friends find you.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+91",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)).padding(16.dp)
                    )
                    Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color(0xFFE5E7EB)))
                    androidx.compose.foundation.text.BasicTextField(
                        value = mobile,
                        onValueChange = {
                            if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                mobile = it
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 16.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f).padding(16.dp),
                        decorationBox = { innerTextField ->
                            if (mobile.isEmpty()) {
                                Text("9876543210", color = Color(0xFFD1D5DB), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Privacy selector mock
                Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).background(Color(0xFFEFF6FF), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Number Privacy", fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Who can see this number on your profile?", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(12.dp)).padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("My Contacts Only", fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 14.sp)
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        if (mobile.length != 10) {
                            localError = "Please enter a valid 10-digit number"
                        } else {
                            navController.navigate("onboarding_bio")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
        
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            DynamicIslandLocal(localError, MessageType.ERROR)
        }
    }
}

@Composable
fun OnboardingBioScreen(navController: NavController, authViewModel: AuthViewModel) {
    var bio by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(localError) {
        if (localError.isNotEmpty()) {
            delay(2500)
            localError = ""
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
            OnboardingHeader(step = 4) { navController.popBackStack() }
            
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Write a short bio", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827)))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tell your friends a little about yourself.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Living life, one cup of coffee at a time ☕", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 14.sp),
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        if (bio.isBlank()) {
                            localError = "Please write a short bio"
                            return@Button
                        }
                        isLoading = true
                        authViewModel.showMessage("Setting up profile...", MessageType.LOADING)
                        coroutineScope.launch {
                            delay(1500)
                            authViewModel.showMessage("Profile Setup Complete!|Welcome to ChatVerse", MessageType.SUCCESS)
                            delay(1500)
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFFE81CFF), Color(0xFF41B5FF))), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Finish Setup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
        
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            DynamicIslandNotification(authViewModel)
            DynamicIslandLocal(localError, MessageType.ERROR)
        }
    }
}
