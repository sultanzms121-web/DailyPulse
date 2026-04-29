package com.dailypulse.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper // 🌟 Added for Empty State
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailypulse.ui.components.ExploreItem
import com.dailypulse.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: HomeViewModel,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToSource: (String, String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    val trendingArticles by viewModel.getArticles(
        category = "Trending",
        language = selectedLanguage
    ).collectAsState(initial = emptyList())

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshNewsFromApi("Trending")
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                DockedSearchBar(
                    query = "",
                    onQueryChange = {},
                    onSearch = {},
                    active = false,
                    onActiveChange = { onNavigateToSearch() },
                    placeholder = { Text("Search news, topics, and more...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onNavigateToSearch() }
                ) {}

                Text(
                    text = "Trending Now",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    ) { padding ->
        // 🌟 PullToRefreshBox requires Material3 1.3.0+
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshNewsFromApi("Trending") },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (trendingArticles.isEmpty() && !isRefreshing) {
                // 🌟 Defined at the bottom of this file
                ExploreEmptyState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(trendingArticles) { article ->
                        ExploreItem(
                            article = article,
                            onClick = {
                                viewModel.onArticleClicked(article.contentUrl)
                                onNavigateToArticle(article.contentUrl)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 🌟 SHARED UI COMPONENT
 * This clears the "Unresolved reference" error you were getting.
 */
@Composable
fun ExploreEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Newspaper,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No trending articles found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Pull down to refresh or try again later.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}