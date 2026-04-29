package com.dailypulse.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dailypulse.model.NewsArticle
import com.dailypulse.model.Post
import com.dailypulse.model.User as MyUser
import com.dailypulse.viewmodel.HomeViewModel
import com.dailypulse.viewmodel.AuthViewModel
import com.dailypulse.viewmodel.SocialViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private val DeepNavy = Color(0xFF002147)
val homeCategories = listOf("All", "International", "Technology", "Sports", "Business", "Health", "Entertainment", "Science")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    authViewModel: AuthViewModel,
    socialViewModel: SocialViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToSource: (String, String) -> Unit,
    onNavigateToPublicProfile: (String) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val firebaseUser = authViewModel.getCurrentUser()
    val isLoggedIn = firebaseUser != null

    // --- 📊 UI STATE ---
    var selectedCategory by remember { mutableStateOf("All") }
    var isBroadcastOpen by remember { mutableStateOf(false) }
    var userData by remember { mutableStateOf<MyUser?>(null) }
    val followedPosts = remember { mutableStateListOf<Post>() }

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val refreshState by viewModel.isRefreshing.collectAsState()
    val articles by viewModel.getArticles(selectedCategory, selectedLanguage).collectAsState(initial = emptyList())

    // 🚀 REAL-TIME LISTENERS
    LaunchedEffect(firebaseUser) {
        if (isLoggedIn) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(firebaseUser!!.uid)
                .addSnapshotListener { userSnapshot, _ ->
                    userData = userSnapshot?.toObject(MyUser::class.java)
                    val following = userSnapshot?.get("following") as? List<String> ?: emptyList()

                    if (following.isNotEmpty()) {
                        db.collection("posts")
                            .whereIn("authorId", following.take(10))
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .addSnapshotListener { postSnapshot, _ ->
                                if (postSnapshot != null) {
                                    followedPosts.clear()
                                    followedPosts.addAll(postSnapshot.toObjects(Post::class.java))
                                }
                            }
                    } else {
                        followedPosts.clear()
                    }
                }
        }
    }

    LaunchedEffect(selectedCategory) { listState.animateScrollToItem(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DailyPulse", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onNavigateToSearch) { Icon(Icons.Default.Search, null) }
                    IconButton(onClick = onNavigateToProfile) { Icon(Icons.Default.AccountCircle, null) }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {

            // --- 🏁 CATEGORY SELECTOR ---
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(homeCategories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = refreshState,
                onRefresh = { viewModel.manualRefresh(selectedCategory) }
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // --- 🎙️ BROADCAST PROMPT ---
                    if (isLoggedIn && selectedCategory == "All") {
                        item {
                            InlineBroadcastBar(
                                userPhoto = firebaseUser?.photoUrl?.toString(),
                                onInputClick = { isBroadcastOpen = true }
                            )
                        }
                    }

                    // --- 🔄 UNIFIED NEWS STREAM ---

                    // 1. Social Posts (Displayed only in "All" view)
                    if (selectedCategory == "All" && followedPosts.isNotEmpty()) {
                        items(followedPosts) { post ->
                            UserTimelineCard(
                                post = post,
                                onClick = { onNavigateToPostDetail(post.id) }
                            )
                        }
                    }

                    // 2. API Global News
                    items(items = articles, key = { "${it.contentUrl}_${it.category}" }) { article ->
                        SocialNewsCard(
                            article = article,
                            isLoggedIn = isLoggedIn,
                            onNavigateToArticle = onNavigateToArticle,
                            onNavigateToSource = onNavigateToSource,
                            onToggleBookmark = { viewModel.toggleBookmark(article) }
                        )
                    }
                }
            }
        }
    }

    // --- ✍️ BROADCAST DIALOG ---
    if (isBroadcastOpen && firebaseUser != null) {
        NewsBroadcastDialog(
            userPhoto = firebaseUser.photoUrl?.toString(),
            userName = firebaseUser.displayName ?: "Reporter",
            onDismiss = { isBroadcastOpen = false },
            onPost = { headline, details, category ->
                socialViewModel.postNews(
                    userId = firebaseUser.uid,
                    userName = firebaseUser.displayName ?: "Reporter",
                    userHandle = userData?.username ?: "user",
                    userPhoto = firebaseUser.photoUrl.toString(),
                    title = headline,
                    content = details
                )
                isBroadcastOpen = false
                Toast.makeText(context, "News reported!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// --- 🧱 LOCAL HELPER COMPONENTS ---

@Composable
fun InlineBroadcastBar(userPhoto: String?, onInputClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { onInputClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = userPhoto, contentDescription = null, modifier = Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(12.dp))
            Text("Report a news update...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.RssFeed, null, tint = DeepNavy)
        }
    }
}

@Composable
fun SocialNewsCard(
    article: NewsArticle,
    isLoggedIn: Boolean,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToSource: (String, String) -> Unit,
    onToggleBookmark: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, modifier = Modifier.size(32.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(article.sourceName.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = article.sourceName,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToSource(article.sourceId ?: "", article.sourceName) }
                )
            }

            AsyncImage(
                model = article.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(240.dp).clickable { onNavigateToArticle(article.contentUrl) },
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(article.title, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text(article.summary, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    if (isLoggedIn) {
                        IconButton(onClick = { /* Like */ }) { Icon(Icons.Outlined.FavoriteBorder, null) }
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            tint = if (article.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { onNavigateToArticle(article.contentUrl) }) {
                        Text("READ REPORT", fontWeight = FontWeight.ExtraBold, color = DeepNavy)
                    }
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
        }
    }
}