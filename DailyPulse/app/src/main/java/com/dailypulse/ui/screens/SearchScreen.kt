package com.dailypulse.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailypulse.ui.components.NewsCard
import com.dailypulse.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToSource: (String, String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val matchedSource by viewModel.matchedSource.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message -> snackbarHostState.showSnackbar(message) }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (totalItems > 0 && lastVisibleIndex >= totalItems - 2 && !isLoading) {
                    viewModel.loadNextSearchPage()
                }
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        placeholder = { Text("Search in any language...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChanged(""); focusManager.clearFocus() }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading && searchResults.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                searchQuery.isEmpty() -> {
                    SearchPlaceholder(Modifier.align(Alignment.Center))
                }

                searchResults.isEmpty() && !isLoading -> {
                    Text(
                        text = "No results found for \"$searchQuery\"",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 🌟 SECTION 1: THE NEWS PAGE RESULT (Fixed Navigation)
                        matchedSource?.let { source ->
                            item {
                                Text(
                                    text = "News Pages",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                SourceResultCard(
                                    sourceName = source.sourceName,
                                    onClick = {
                                        // 🌟 FIX: Pass the sourceName if ID is null to ensure it moves to the page
                                        val idToUse = source.sourceId ?: source.sourceName
                                        onNavigateToSource(idToUse, source.sourceName)
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)
                                Text(
                                    text = "Recent Articles",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // SECTION 2: INDIVIDUAL ARTICLES
                        items(items = searchResults, key = { it.contentUrl }) { article ->
                            NewsCard(
                                article = article,
                                isBookmarked = article.isBookmarked,
                                onClick = {
                                    viewModel.onArticleClicked(article.contentUrl)
                                    onNavigateToArticle(article.contentUrl)
                                },
                                onBookmarkToggle = { viewModel.toggleBookmark(article) },
                                onSourceClick = onNavigateToSource
                            )
                        }

                        if (isLoading && searchResults.isNotEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SourceResultCard(sourceName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // 🌟 Ensures the entire card is a button
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = sourceName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sourceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Official News Channel • Tap to View Profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SearchPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Discover the latest news",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Type keywords to search for articles in any language.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}