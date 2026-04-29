package com.dailypulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

@Composable
fun OceanWaves() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // --- Layer 1: Deep Base Wave ---
        val path1 = Path().apply {
            moveTo(0f, height * 0.75f)
            quadraticBezierTo(
                width * 0.25f, height * 0.70f,
                width * 0.5f, height * 0.80f
            )
            quadraticBezierTo(
                width * 0.75f, height * 0.90f,
                width, height * 0.85f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        // Deep midnight blue tone
        drawPath(path1, color = Color(0xFF001529).copy(alpha = 0.5f))

        // --- Layer 2: Top Accent Wave ---
        val path2 = Path().apply {
            moveTo(0f, height * 0.85f)
            quadraticBezierTo(
                width * 0.35f, height * 0.80f,
                width * 0.6f, height * 0.90f
            )
            quadraticBezierTo(
                width * 0.85f, height * 0.95f,
                width, height * 0.88f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        // Lighter navy tone
        drawPath(path2, color = Color(0xFF003366).copy(alpha = 0.3f))
    }
}