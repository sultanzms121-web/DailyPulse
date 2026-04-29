package com.dailypulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- 🌟 COIL 3.0 IMPORTS ---
import coil3.compose.SubcomposeAsyncImage // ✅ CORRECT (Coil 3.0)
import coil3.request.ImageRequest           // ✅ CORRECT (Coil 3.0)
import coil3.request.crossfade              // ✅ Needed for .crossfade(true)

// --- DATA & UTILS ---
import com.dailypulse.model.NewsArticle
import com.dailypulse.util.DateUtils
import java.net.URLEncoder

/**
 * 🌟 THE NEWS CARD
 * Displays a single article with AI-image fallback and source-profile navigation.
 */
@Composable
fun NewsCard(
    article: NewsArticle,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onSourceClick: (String, String) -> Unit
) {
    val isRead = article.isRead
    val contentAlpha = if (isRead) 0.65f else 1f
    val containerColor = if (isRead)
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    else MaterialTheme.colorScheme.surface

    // 1. Memoized Date Logic
    val timeAgo = remember(article.pubDate) {
        DateUtils.getTimeAgo(article.pubDate)
    }

    // 2. Memoized AI Image Logic (Prevents lag during scrolling)
    val displayImage = remember(article.imageUrl, article.title) {
        if (!article.imageUrl.isNullOrBlank()) {
            article.imageUrl
        } else {
            val sanitized = article.title.replace(Regex("[^a-zA-Z0-9 ]"), "")
            val encoded = URLEncoder.encode(sanitized, "UTF-8")
            // Still using AI generation for fallback, just removed the visual badge indicating it.
            "https://image.pollinations.ai/prompt/${encoded}+cinematic+news+photography?width=800&height=500&nologo=true&seed=${article.contentUrl.hashCode()}&model=flux"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clickable(onClick = onClick)
            .alpha(contentAlpha),
        shape = RoundedCornerShape(20.dp), // Modern look
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRead) 0.dp else 3.dp)
    ) {
        Column {
            // --- IMAGE HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(displayImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        }
                    }
                )
                // 🌟 REMOVED: Golden AI Badge and background gradient overlay
            }

            // --- CONTENT SECTION ---
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Interactive Source Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    article.sourceId?.let { id -> onSourceClick(id, article.sourceName) }
                                }
                        ) {
                            if (isRead) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp).padding(end = 4.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = article.sourceName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = timeAgo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    IconButton(onClick = onBookmarkToggle, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isRead) FontWeight.SemiBold else FontWeight.Bold,
                        lineHeight = 22.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Metadata Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${article.readTimeMinutes} min read",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    if (article.category.isNotBlank() && article.category != "All") {
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            // 🌟 UPDATED: Used standard secondary container color instead of gold
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = article.category,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                // 🌟 UPDATED: Used standard on-secondary container color
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}