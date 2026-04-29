package com.dailypulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailypulse.ui.components.NewsCard
import com.dailypulse.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceDetailScreen(
    sourceId: String,
    sourceName: String,
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToArticle: (String) -> Unit
) {
    // Observe articles and refresh state from the ViewModel
    val articles by viewModel.getArticlesBySource(sourceId).collectAsState(initial = emptyList())
    val isFollowing by viewModel.isFollowing(sourceId).collectAsState(initial = false)
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // 🌟 THE FIX: Trigger a news refresh for this source when the screen opens
    LaunchedEffect(sourceId) {
        if (articles.isEmpty()) {
            // We use the source name as the search query to fetch the latest news
            viewModel.refreshNewsFromApi(sourceName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sourceName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- SOURCE HEADER ---
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Circle
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(70.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = sourceName.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(text = sourceName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Official News Source", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (isFollowing) viewModel.unfollowSource(sourceId)
                            else viewModel.followSource(sourceId, sourceName)
                        },
                        colors = if (isFollowing) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.buttonColors(),
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text(if (isFollowing) "Following" else "Follow")
                    }
                }

                Text(
                    text = "Latest from $sourceName",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(Modifier.padding(bottom = 8.dp), thickness = 0.5.dp)
            }

            // --- LOADING STATE ---
            if (isRefreshing && articles.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // --- ARTICLES LIST ---
            items(articles, key = { it.contentUrl }) { article ->
                NewsCard(
                    article = article,
                    isBookmarked = article.isBookmarked,
                    onClick = {
                        viewModel.onArticleClicked(article.contentUrl)
                        onNavigateToArticle(article.contentUrl)
                    },
                    onBookmarkToggle = { viewModel.toggleBookmark(article) },
                    onSourceClick = { _, _ -> } // Already on the source page
                )
            }
        }
    }
}