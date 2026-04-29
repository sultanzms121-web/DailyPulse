package com.dailypulse.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- DEEP NAVY OCEAN PALETTE ---
val DeepNavy = Color(0xFF002147)      // Main Brand Navy
val MidnightSea = Color(0xFF001529)   // Darker for accents/text
val NavyHighlight = Color(0xFFE3F2FD) // Soft blue for backgrounds

// --- DARK PALETTE (Midnight Ocean Aesthetic) ---
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),      // Accessible blue for dark mode
    secondary = DeepNavy,
    background = Color(0xFF080C10),   // Navy-tinted Black
    surface = Color(0xFF121920),      // Navy-Grey Cards
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

// --- LIGHT PALETTE (Deep Navy & Pure White) ---
private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,               // YOUR BRAND COLOR
    secondary = MidnightSea,
    background = Color.White,         // Clean white background
    surface = Color.White,
    onPrimary = Color.White,          // White text on Navy buttons
    onBackground = Color(0xFF001529), // Navy text on white background
    onSurface = Color(0xFF001529)
)

@Composable
fun DailyPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Kept false to protect your Navy brand
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // StatusBar follows the primary brand color
            window.statusBarColor = colorScheme.primary.toArgb()

            // Makes status bar icons dark in Light Mode, light in Dark Mode
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}