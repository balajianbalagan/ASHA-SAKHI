package com.littleb01s.ashasakhichat.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littleb01s.ashasakhichat.presentation.MessageStatus

@Composable
fun StatusChip(
    status: MessageStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_animation")
    
    // Shining animation
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    // Pulse animation for searching
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Animated dots for generating response
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    
    val statusInfo = when (status) {
        MessageStatus.SEARCHING_DATA -> {
            StatusInfo(
                backgroundColor = Color(0xFF2196F3), // Blue
                textColor = Color.White,
                text = "🔍 Searching data...",
                showAnimatedDots = false
            )
        }
        MessageStatus.GENERATING_RESPONSE -> {
            StatusInfo(
                backgroundColor = Color(0xFF4CAF50), // Green
                textColor = Color.White,
                text = "⚡ Generating detailed response",
                showAnimatedDots = true
            )
        }
        MessageStatus.COMPLETED -> {
            StatusInfo(
                backgroundColor = Color(0xFF9E9E9E), // Gray
                textColor = Color.White,
                text = "✅ Completed",
                showAnimatedDots = false
            )
        }
        MessageStatus.NONE -> {
            StatusInfo(
                backgroundColor = Color.Transparent,
                textColor = Color.Transparent,
                text = "",
                showAnimatedDots = false
            )
        }
    }
    
    if (status != MessageStatus.NONE) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            statusInfo.backgroundColor,
                            statusInfo.backgroundColor.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            // Shining effect overlay
            if (status == MessageStatus.SEARCHING_DATA || status == MessageStatus.GENERATING_RESPONSE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                start = androidx.compose.ui.geometry.Offset(shimmerOffset, 0f),
                                end = androidx.compose.ui.geometry.Offset(shimmerOffset + 100f, 0f)
                            )
                        )
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = statusInfo.text,
                    color = statusInfo.textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                
                // Animated dots for generating response
                if (statusInfo.showAnimatedDots) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = ".",
                            color = statusInfo.textColor.copy(alpha = dot1Alpha),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ".",
                            color = statusInfo.textColor.copy(alpha = dot2Alpha),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ".",
                            color = statusInfo.textColor.copy(alpha = dot3Alpha),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private data class StatusInfo(
    val backgroundColor: Color,
    val textColor: Color,
    val text: String,
    val showAnimatedDots: Boolean
) 