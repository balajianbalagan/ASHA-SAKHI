package com.littleb01s.ashasakhichat.presentation.screens.riskanalysis

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RiskSpeedometer(
    riskLevel: String?,
    modifier: Modifier = Modifier
) {
    // Define fixed colors for risk levels
    val lowRiskColor = Color(0xFF4CAF50)  // Green
    val midRiskColor = Color(0xFFFFC107)  // Yellow/Amber
    val highRiskColor = Color(0xFFF44336) // Red
    
    // Get the current risk color and description
    val (riskColor, riskDescription) = when(riskLevel?.lowercase()) {
        "low risk" -> Pair(lowRiskColor, "Low risk - Continue regular monitoring")
        "mid risk" -> Pair(midRiskColor, "Medium risk - Increased monitoring recommended")
        "high risk" -> Pair(highRiskColor, "High risk - Immediate medical attention required")
        else -> Pair(MaterialTheme.colorScheme.onSurfaceVariant, "Not assessed")
    }
    
    // Define fixed angles for each risk level - adjust these values
    val needleAngle = when(riskLevel?.lowercase()) {
        "low risk" -> 210f  // Center of low risk section
        "mid risk" -> 270f  // Center of mid risk section
        "high risk" -> 330f // Center of high risk section - this needs to be fixed
        else -> 180f        // Default position
    }
    
    // Create an animatable for smooth animation with initial value matching the default angle
    val animatedAngle = remember { Animatable(180f) }
    
    // Always animate from the leftmost (180°) position for a smooth sweep
    LaunchedEffect(riskLevel) {
        animatedAngle.snapTo(180f)
        animatedAngle.animateTo(
            targetValue = needleAngle,
            animationSpec = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Gray.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val center = Offset(width / 2f, height * 0.8f)
                    val radius = minOf(width / 2f, height) * 0.8f
                    
                    // Draw outer shadow for the gauge
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.2f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius - 4f, center.y - radius - 4f),
                        size = Size(radius * 2 + 8f, radius * 2 + 8f),
                        style = Stroke(width = 48f, cap = StrokeCap.Round)
                    )
                    
                    // Draw the arc background
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 40f, cap = StrokeCap.Round)
                    )
                    
                    // Draw the colored sections
                    // Low risk section (green)
                    drawArc(
                        color = lowRiskColor,
                        startAngle = 180f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 40f, cap = StrokeCap.Round)
                    )
                    
                    // Medium risk section (yellow)
                    drawArc(
                        color = midRiskColor,
                        startAngle = 240f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 40f, cap = StrokeCap.Round)
                    )
                    
                    // High risk section (red)
                    drawArc(
                        color = highRiskColor,
                        startAngle = 300f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 40f, cap = StrokeCap.Round)
                    )
                    
                    // Draw section dividers
                    for (i in 1..2) {
                        val angle = PI + (PI * i / 3)
                        val dividerStart = Offset(
                            x = center.x + (radius - 60f) * cos(angle).toFloat(),
                            y = center.y + (radius - 60f) * sin(angle).toFloat()
                        )
                        val dividerEnd = Offset(
                            x = center.x + (radius - 10f) * cos(angle).toFloat(),
                            y = center.y + (radius - 10f) * sin(angle).toFloat()
                        )
                        
                        drawLine(
                            color = Color.White,
                            start = dividerStart,
                            end = dividerEnd,
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                    }
                    
                    // Draw tick marks
                    val tickCount = 9
                    for (i in 0..tickCount) {
                        if (i != 0 && i != 3 && i != 6 && i != 9) {
                            val angle = PI + (PI * i / tickCount)
                            val tickStart = Offset(
                                x = center.x + (radius - 50f) * cos(angle).toFloat(),
                                y = center.y + (radius - 50f) * sin(angle).toFloat()
                            )
                            val tickEnd = Offset(
                                x = center.x + (radius - 30f) * cos(angle).toFloat(),
                                y = center.y + (radius - 30f) * sin(angle).toFloat()
                            )
                            
                            drawLine(
                                color = Color.White.copy(alpha = 0.7f),
                                start = tickStart,
                                end = tickEnd,
                                strokeWidth = 2f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                    
                    // Draw the needle
                    if (riskLevel != null) {
                        // Draw needle shadow
                        rotate(animatedAngle.value) {
                            drawLine(
                                color = Color.Black.copy(alpha = 0.3f),
                                start = Offset(center.x + 2f, center.y + 2f),
                                end = Offset(center.x, center.y - radius + 30f),
                                strokeWidth = 10f,
                                cap = StrokeCap.Round
                            )
                        }
                        
                        // Draw needle with appropriate color
                        rotate(animatedAngle.value) {
                            drawLine(
                                color = riskColor,
                                start = Offset(center.x, center.y),
                                end = Offset(center.x, center.y - radius + 30f),
                                strokeWidth = 8f,
                                cap = StrokeCap.Round
                            )
                        }
                        
                        // Draw needle tip
                        rotate(animatedAngle.value) {
                            drawCircle(
                                color = riskColor,
                                radius = 6f,
                                center = Offset(center.x, center.y - radius + 30f)
                            )
                        }
                        
                        // Draw center circle shadow
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.2f),
                            radius = 18f,
                            center = Offset(center.x + 2f, center.y + 2f)
                        )
                        
                        // Draw center circle
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.LightGray, Color.DarkGray),
                                center = center,
                                radius = 15f
                            ),
                            radius = 15f,
                            center = center
                        )
                        
                        // Draw center dot
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = center
                        )
                    }
                    
                    // Draw labels with backgrounds
                    val labelBackgroundRadius = 30f
                    
                    // Low risk label
                    val lowRiskPos = Offset(
                        x = center.x - radius + 50f,
                        y = center.y - radius / 2 + 10f
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = labelBackgroundRadius,
                        center = lowRiskPos
                    )
                    
                    // Mid risk label
                    val midRiskPos = Offset(
                        x = center.x,
                        y = center.y - radius + 40f
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = labelBackgroundRadius,
                        center = midRiskPos
                    )
                    
                    // High risk label
                    val highRiskPos = Offset(
                        x = center.x + radius - 50f,
                        y = center.y - radius / 2 + 10f
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = labelBackgroundRadius,
                        center = highRiskPos
                    )
                    
                    // Create Paint objects for text
                    val labelTextSize = 24.sp.toPx()
                    val blackColor = Color.Black.toArgb()
                    val lowPaint = Paint().apply {
                        color = blackColor
                        textSize = labelTextSize
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                        setShadowLayer(2f, 0f, 2f, Color.Black.copy(alpha = 0.3f).toArgb())
                    }
                    val midPaint = Paint().apply {
                        color = blackColor
                        textSize = labelTextSize
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                        setShadowLayer(2f, 0f, 2f, Color.Black.copy(alpha = 0.3f).toArgb())
                    }
                    val highPaint = Paint().apply {
                        color = blackColor
                        textSize = labelTextSize
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                        setShadowLayer(2f, 0f, 2f, Color.Black.copy(alpha = 0.3f).toArgb())
                    }
                    
                    // Draw text
                    drawContext.canvas.nativeCanvas.apply {
                        drawText("LOW", lowRiskPos.x, lowRiskPos.y + 8f, lowPaint)
                        drawText("MID", midRiskPos.x, midRiskPos.y + 8f, midPaint)
                        drawText("HIGH", highRiskPos.x, highRiskPos.y + 8f, highPaint)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Explicit risk text below the gauge
            val riskText = when (riskLevel?.lowercase()) {
                "low risk" -> "LOW RISK"
                "mid risk" -> "MID RISK"
                "high risk" -> "HIGH RISK"
                else -> "NOT ASSESSED"
            }
            Text(
                text = riskText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Display risk level card
            if (riskLevel != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = riskColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, riskColor.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(riskColor)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = riskLevel.replaceFirstChar { it.uppercase() },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = riskColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = riskDescription,
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Text(
                    text = "Not Assessed",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 