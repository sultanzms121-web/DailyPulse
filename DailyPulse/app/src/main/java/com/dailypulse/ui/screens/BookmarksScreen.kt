package com.dailypulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailypulse.ui.components.NewsCard
import com.dailypulse.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToSource: (String, String) -> Unit
) {
    // 🌟 Observe the bookmarked articles from ViewModel
    val bookmarkedArticles by viewModel.bookmarkedArticles.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Saved Articles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (bookmarkedArticles.isEmpty()) {
                // 🌟 Empty State for Bookmarks
                BookmarksEmptyState()
            } else {
                // 🌟 List of Saved News
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = bookmarkedArticles,
                        key = { "${it.contentUrl}_bookmark" }
                    ) { article ->
                        NewsCard(
                            article = article,
                            isBookmarked = true, // Always true here
                            onClick = {
                                viewModel.onArticleClicked(article.contentUrl)
                                onNavigateToArticle(article.contentUrl)
                            },
                            onBookmarkToggle = { viewModel.toggleBookmark(article) },
                            onSourceClick = { id, name -> onNavigateToSource(id, name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarksEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No bookmarks yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Articles you bookmark will appear here for easy access later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}