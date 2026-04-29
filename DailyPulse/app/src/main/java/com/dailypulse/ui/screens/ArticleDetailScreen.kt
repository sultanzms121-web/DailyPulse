package com.dailypulse.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 🌟 THE CRITICAL FIX: Updated to Coil 3.0 packages
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

import com.dailypulse.model.NewsArticle
import com.dailypulse.util.DateUtils
import com.dailypulse.util.openNewsArticle
import com.dailypulse.viewmodel.ArticleDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    article: NewsArticle,
    languageCode: String,
    viewModel: ArticleDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSource: (String, String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val summaryText by viewModel.displaySummary.collectAsState()
    val displayImageUrl by viewModel.displayImageUrl.collectAsState()

    var isBookmarked by remember { mutableStateOf(article.isBookmarked) }

    // Brand Color: Signature Golden
    val goldenBrand = Color(0xFFFFD700)

    LaunchedEffect(article.contentUrl) {
        viewModel.loadArticleContent(article, languageCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check this out on Daily Pulse: ${article.contentUrl}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(onClick = {
                        isBookmarked = !isBookmarked
                        // viewModel.toggleBookmark(article) // Toggle in local DB
                    }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) goldenBrand else LocalContentColor.current
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- CINEMATIC HEADER ---
            Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                NewsDetailHeader(imageUrl = displayImageUrl, title = article.title)
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Category Tag
                Surface(
                    color = goldenBrand.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = article.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB8860B), // Dark Gold
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Source & Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            article.sourceId?.let { id -> onNavigateToSource(id, article.sourceName) }
                        }
                    ) {
                        Text(
                            text = article.sourceName,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " • ${DateUtils.getTimeAgo(article.pubDate)}",
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Text(
                        "${article.readTimeMinutes} min read",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), thickness = 0.5.dp)

                // --- AI SUMMARY SECTION ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = goldenBrand,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "AI Pulse Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(summaryText))
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedContent(
                    targetState = summaryText,
                    transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(600)) },
                    label = "SummaryFade"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 30.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Read Full Article Button
                Button(
                    onClick = { openNewsArticle(context, article.contentUrl) },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("READ FULL STORY", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun NewsDetailHeader(imageUrl: String, title: String) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl.takeIf { it.isNotBlank() })
                .crossfade(true)
                .build(),
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                }
            },
            error = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Newspaper,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )
                }
            },
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for cinematic depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 400f
                    )
                )
        )

        if (imageUrl.contains("pollinations") || imageUrl.isBlank()) {
            Surface(
                color = Color(0xFFFFD700), // Signature Golden
                contentColor = Color.Black,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Text(
                    "AI REIMAGINED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}