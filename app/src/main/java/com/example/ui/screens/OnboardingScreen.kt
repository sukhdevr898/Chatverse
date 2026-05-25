package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        kotlinx.coroutines.delay(2000)
        navController.navigate("onboarding") {
            popUpTo("splash") { inclusive = true }
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepBlack, Graphite)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Orb behind
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonBlue.copy(alpha = 0.2f), Color.Transparent),
                        radius = 300f
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale).alpha(alpha)
        ) {
            Icon(
                imageVector = Icons.Filled.BubbleChart,
                contentDescription = "Logo",
                tint = NeonBlue,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ChatVerse",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = PureWhite
            )
        }

        Text(
            text = "Connecting People Beyond Limits",
            style = MaterialTheme.typography.bodyMedium,
            color = SoftGray,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(alpha)
        )
    }
}

@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPage(page = page)
        }

        // Top Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, end = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Skip",
                color = SoftGray,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.clickable {
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }.padding(8.dp)
            )
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp)
                    val color by animateColorAsState(targetValue = if (isSelected) NeonBlue else SoftDark)
                    
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Next / Get Started Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricPurple, NeonBlue)
                        )
                    )
                    .clickable {
                        coroutineScope.launch {
                            if (pagerState.currentPage < 2) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                navController.navigate("login") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }
                    }
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (pagerState.currentPage == 2) "Get Started" else "Next",
                    color = PureWhite,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    val title = when (page) {
        0 -> "Chat Without Limits"
        1 -> "Crystal Clear Calls"
        else -> "Private & Encrypted"
    }
    
    val subtitle = when (page) {
        0 -> "Experience ultra-fast messaging with a futuristic social experience."
        1 -> "High-quality voice and video communication with real-time reactions."
        else -> "Your conversations stay protected with advanced encryption."
    }

    val icon = when (page) {
        0 -> Icons.Filled.Forum
        1 -> Icons.Filled.Videocam
        else -> Icons.Filled.Shield
    }
    
    val iconColor = when (page) {
        0 -> PinkGradient
        1 -> CyanGlow
        else -> NeonBlue
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Floating icon illustration
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(iconColor.copy(alpha = 0.2f), Color.Transparent),
                        radius = 300f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Graphite.copy(alpha = 0.8f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = PureWhite,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = SoftGray,
            textAlign = TextAlign.Center
        )
    }
}
