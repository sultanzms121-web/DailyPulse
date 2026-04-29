package com.dailypulse.ui.components

// 🌟 ESSENTIAL COMPOSE IMPORTS
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 🌟 IMAGE LOADING IMPORTS (Coil)
import coil3.compose.SubcomposeAsyncImage // ✅ CORRECT (Coil 3.0)
import coil3.request.ImageRequest           // ✅ CORRECT (Coil 3.0)
import coil3.request.crossfade              // ✅ Needed for .crossfade(true)
// 🌟 YOUR DATA MODEL
import com.dailypulse.model.NewsArticle

@Composable
fun ExploreItem(
    article: NewsArticle,
    onClick: () -> Unit
) {
    val goldenGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFD700).copy(alpha = 0.5f), Color.Transparent)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f)))
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize().background(goldenGradient))
                }
            )

            // Bottom Gradient for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = article.category.uppercase(),
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = article.sourceName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}