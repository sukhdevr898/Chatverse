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
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
    val gradientColors = listOf(Color(0xFFE81CFF), Color(0xFF8E44FF), Color(0xFF41B5FF))
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
    
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) {
            drawCircle(
                color = Color(0xFFE9D5FF).copy(alpha = 0.5f), // Purple-200
                radius = 500f,
                center = Offset(0f, 0f)
            )
            drawCircle(
                color = Color(0xFFBFDBFE).copy(alpha = 0.5f), // Blue-200
                radius = 600f,
                center = Offset(size.width, size.height * 0.6f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Logo
            ChatVerseLogo(modifier = Modifier.size(96.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Welcome to\nChatVerse",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Connect securely and seamlessly.\nSign in to continue.",
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.97f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "scale"
            )

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
                                    navController.navigate("main") {
                                        popUpTo("auth") { inclusive = true }
                                    }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .scale(scale)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x33000000), ambientColor = Color(0x11000000))
                    .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp),
                interactionSource = interactionSource
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF4B5563), strokeWidth = 3.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = "Google", tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Continue with Google", color = Color(0xFF1F2937), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = buildAnnotatedString {
                    append("By continuing, you agree to our ")
                    withStyle(style = SpanStyle(color = Color(0xFF9333EA), fontWeight = FontWeight.Bold)) {
                        append("Terms")
                    }
                    append(" and ")
                    withStyle(style = SpanStyle(color = Color(0xFF9333EA), fontWeight = FontWeight.Bold)) {
                        append("Privacy\nPolicy")
                    }
                    append(".")
                },
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
                lineHeight = 20.sp
            )
        }
        
        // Island handled by MainActivity
    }
}

@Composable
fun OnboardingHeader(step: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onBack,
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
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
fun OnboardingNameScreen(navController: NavController, authViewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    
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
                            authViewModel.showMessage("Please enter your name", MessageType.ERROR)
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
        
        // Island handled by MainActivity
    }
}

@Composable
fun OnboardingDobScreen(navController: NavController, authViewModel: AuthViewModel) {
    var dob by remember { mutableStateOf("") }
    
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
                            authViewModel.showMessage("Please enter a valid date", MessageType.ERROR)
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
        
        // Island handled by MainActivity
    }
}

@Composable
fun OnboardingMobileScreen(navController: NavController, authViewModel: AuthViewModel) {
    var mobile by remember { mutableStateOf("") }
    
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
                            authViewModel.showMessage("Please enter a valid 10-digit number", MessageType.ERROR)
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
        
        // Island handled by MainActivity
    }
}

@Composable
fun OnboardingBioScreen(navController: NavController, authViewModel: AuthViewModel) {
    var bio by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    
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
                
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.97f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "scale"
                )

                Button(
                    onClick = {
                        if (bio.isBlank()) {
                            authViewModel.showMessage("Please write a short bio", MessageType.ERROR)
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
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp).scale(scale).shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFA855F7), ambientColor = Color(0xFFA855F7)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading,
                    interactionSource = interactionSource
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
        
        // Island handled by MainActivity
    }
}
