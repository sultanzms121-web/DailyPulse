package com.dailypulse.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image // 🌟 Added for the logo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip // 🌟 To ensure the image stays circular
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource // 🌟 Needed to load your drawable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailypulse.R // 🌟 Make sure to import your R file
import com.dailypulse.ui.components.OceanWaves
import kotlinx.coroutines.delay

private val DeepNavyOcean = Color(0xFF001F3F)

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(2000) // 2 seconds display time
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavyOcean),
        contentAlignment = Alignment.Center
    ) {
        OceanWaves() // 🌊 The wave background from your screenshot

        Column(
            modifier = Modifier.padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 👤 CENTRAL LOGO CONTAINER ---
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(170.dp) // This controls the overall circle size
                    .alpha(alpha.value),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo), // 👈 Or your custom logo name
                        contentDescription = "DailyPulse Logo",
                        modifier = Modifier
                            .fillMaxSize() // 🌟 This makes the icon fill the circle
                            .clip(CircleShape), // 🌟 Keeps the image perfectly circular
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop // 🌟 Scales it to cover the area
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- 📝 BRANDING ---
            Text(
                text = "Your AI-powered personalized news platform.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Take your daily dose of Truth",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}