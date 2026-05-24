package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AnimatedGradientMeshBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val angle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "angle"
    )
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Neon Blue Orb
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-100).dp)
                .size(400.dp)
                .graphicsLayer {
                    rotationZ = angle.value
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .blur(radius = 120.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                .background(
                    Brush.radialGradient(
                        colors = listOf(DeepIndigo.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(200f, 200f),
                        radius = 400f
                    ),
                    shape = CircleShape
                )
        )

        // Electric Purple Orb
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(500.dp)
                .graphicsLayer {
                    rotationZ = -angle.value
                    scaleX = scale.value * 0.9f
                    scaleY = scale.value * 0.9f
                }
                .blur(radius = 150.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricPurple.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(250f, 250f),
                        radius = 500f
                    ),
                    shape = CircleShape
                )
        )
        
        // Cyan Accent Orb
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 150.dp, y = (-50).dp)
                .size(300.dp)
                .graphicsLayer {
                    rotationZ = angle.value * 1.5f
                    scaleX = scale.value * 1.1f
                    scaleY = scale.value * 1.1f
                }
                .blur(radius = 100.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonBlue.copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(150f, 150f),
                        radius = 300f
                    ),
                    shape = CircleShape
                )
        )
        
        // Dark Overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack.copy(alpha = 0.4f))
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 32.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(GlassWhite)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(2.dp), // Inner glow padding
        content = content
    )
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.5f else 0.1f,
        animationSpec = tween(300), label = "glow"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = if (isFocused) NeonBlue else TextSecondary) },
        leadingIcon = leadingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonBlue,
            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
            focusedContainerColor = PureBlack.copy(alpha = 0.3f),
            unfocusedContainerColor = PureBlack.copy(alpha = 0.2f),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = NeonBlue
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = NeonBlue.copy(alpha = glowAlpha),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
    )
}

@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150), label = "scale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(150), label = "alpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(NeonBlue, ElectricPurple)
                )
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = {
                if (!isLoading) onClick()
            }),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = Color.White.copy(alpha = contentAlpha),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
fun SocialButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(150), label = "scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(56.dp)
            .clip(CircleShape)
            .background(DarkSurface.copy(alpha = 0.8f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}
