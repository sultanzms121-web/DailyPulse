package com.dailypulse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 🎨 Use a darker silver to ensure you can see the difference
private val DeepNavy = Color(0xFF002147)
private val SilverBackground = Color(0xFFE0E0E2)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    // 🌟 FORCE OVERRIDE: Using a Surface with a specific color as the root
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SilverBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 👤 CENTRAL LOGO ---
            Surface(
                shape = CircleShape,
                color = DeepNavy,
                modifier = Modifier.size(160.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Newspaper,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                        Text(
                            text = "DAILYPULSE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Welcome to DailyPulse",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DeepNavy,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your AI-powered personalized news platform.",
                fontSize = 16.sp,
                color = DeepNavy.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
            ) {
                Text("GET STARTED", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}