package com.humblecoders.fintrack

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.fintrack.ui.theme.BudgetBaeDarkGreen
import com.humblecoders.fintrack.ui.theme.BudgetBaeGold
import com.humblecoders.fintrack.ui.theme.BudgetBaeTeal
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigate: () -> Unit
) {
    // Animation states
    var startAnimation by remember { mutableStateOf(false) }
    
    // Logo animation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "logoAlpha"
    )
    
    // Tagline animation
    val taglineAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "taglineAlpha"
    )
    
    val taglineOffset by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 20.dp,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "taglineOffset"
    )
    
    // Description animation
    val descriptionAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "descriptionAlpha"
    )
    
    // Decorative circles animation
    val circle1Scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "circle1Scale"
    )
    
    val circle2Scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow,
            visibilityThreshold = 0.01f
        ),
        label = "circle2Scale"
    )
    
    val circle3Scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessVeryLow,
            visibilityThreshold = 0.01f
        ),
        label = "circle3Scale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
        delay(2500) // Show splash for 2.5 seconds
        onNavigate()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Decorative circles in background
            // Top right - light pink
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-60).dp)
                    .size(200.dp)
                    .scale(circle1Scale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFE5F1).copy(alpha = 0.6f),
                                Color(0xFFFFE5F1).copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // Bottom left - light blue
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-60).dp, y = 60.dp)
                    .size(180.dp)
                    .scale(circle2Scale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE5F3FF).copy(alpha = 0.6f),
                                Color(0xFFE5F3FF).copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // Center right - light purple
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 40.dp, y = 100.dp)
                    .size(150.dp)
                    .scale(circle3Scale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BudgetBaeTeal.copy(alpha = 0.15f),
                                BudgetBaeTeal.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Main content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo with animation
                Row(
                    modifier = Modifier
                        .scale(logoScale)
                        .alpha(logoAlpha),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = "Budget",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = BudgetBaeDarkGreen,
                        letterSpacing = 1.sp
                    )
                    androidx.compose.material3.Text(
                        text = "BAE",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = BudgetBaeGold,
                        letterSpacing = 1.sp
                    )
                }

                // Tagline with animation
                androidx.compose.material3.Text(
                    text = "Your Money Wingmate",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier
                        .alpha(taglineAlpha)
                        .offset(y = taglineOffset)
                        .padding(top = 12.dp),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )

                // Description with animation
                androidx.compose.material3.Text(
                    text = "Making money management\nsimple, fun, and stress-free.",
                    fontSize = 14.sp,
                    color = Color(0xFF999999),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(descriptionAlpha)
                        .padding(top = 32.dp),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

