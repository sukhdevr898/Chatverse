package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
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
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "angle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StitchBackground)
    ) {
        // Primary Container Orb (Top Left)
        Box(
            modifier = Modifier
                .offset(x = (-100).dp, y = (-100).dp)
                .size(600.dp)
                .graphicsLayer {
                    rotationZ = angle.value
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(StitchPrimaryContainer.copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(300f, 300f),
                        radius = 600f
                    ),
                    shape = CircleShape
                )
        )

        // Secondary Container Orb (Bottom Right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(600.dp)
                .graphicsLayer {
                    rotationZ = -angle.value
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(StitchGradient3.copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(300f, 300f),
                        radius = 600f
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .drawBehind {
                // inner shadow emulation conceptually
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                )
            },
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
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    isSuccess: Boolean = false,
    errorMessage: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        }, label = "borderColor"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }, label = "bgColor"
    )

    val labelSize by animateFloatAsState(targetValue = if (isFocused || value.isNotEmpty()) 12f else 16f, label = "labelSize")
    val labelOffset by animateFloatAsState(targetValue = if (isFocused || value.isNotEmpty()) (-12f) else 0f, label = "labelOffset")

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        leadingIcon()
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = labelSize.sp,
                        fontWeight = if (isFocused || value.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.offset(y = labelOffset.dp)
                    )

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                        ),
                        singleLine = true,
                        keyboardOptions = keyboardOptions,
                        visualTransformation = visualTransformation,
                        interactionSource = interactionSource,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (isFocused || value.isNotEmpty()) 8.dp else 0.dp)
                    )
                }

                if (isError) {
                    Icon(Icons.Filled.Error, "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 8.dp).size(20.dp))
                } else if (isSuccess) {
                    Icon(Icons.Filled.CheckCircle, "Success", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(start = 8.dp).size(20.dp))
                }
                
                if (trailingIcon != null) {
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        trailingIcon()
                    }
                }
            }
        }
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
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
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150), label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)
                )
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = {
                if (!isLoading) onClick()
            }),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SocialButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val bgColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
        label = "bg"
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha=0.05f), CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

