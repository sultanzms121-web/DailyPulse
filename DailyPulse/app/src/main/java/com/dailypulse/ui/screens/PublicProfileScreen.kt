package com.dailypulse.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dailypulse.model.Post
import com.dailypulse.model.User
import com.dailypulse.viewmodel.SocialViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Brand Color
private val DeepNavy = Color(0xFF002147)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    targetUserId: String,
    currentUserId: String,
    viewModel: SocialViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit
) {
    val context = LocalContext.current

    // --- 📊 STATE ---
    var targetUser by remember { mutableStateOf<User?>(null) }
    var currentUserProfile by remember { mutableStateOf<User?>(null) }
    val userPosts = remember { mutableStateListOf<Post>() }
    var isLoading by remember { mutableStateOf(true) }

    // --- 🚀 REAL-TIME LISTENERS ---

    // Listener 1: The Target User (Profile Data & Posts)
    LaunchedEffect(targetUserId) {
        val db = FirebaseFirestore.getInstance()

        // Profile Data
        db.collection("users").document(targetUserId).addSnapshotListener { snapshot, _ ->
            targetUser = snapshot?.toObject(User::class.java)
            isLoading = false
        }

        // Broadcast History
        db.collection("posts")
            .whereEqualTo("authorId", targetUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    userPosts.clear()
                    userPosts.addAll(snapshot.toObjects(Post::class.java))
                }
            }
    }

    // Listener 2: The Current User (To track following status in real-time)
    LaunchedEffect(currentUserId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(currentUserId).addSnapshotListener { snapshot, _ ->
            currentUserProfile = snapshot?.toObject(User::class.java)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(targetUser?.name ?: "Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepNavy)
            }
        } else {
            targetUser?.let { user ->
                // 🌟 Logic: Check if targetUserId is inside our following array
                val isFollowing = currentUserProfile?.following?.contains(targetUserId) == true

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // --- 👤 PROFILE HEADER ---
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profile Picture
                            AsyncImage(
                                model = user.photoUrl.ifEmpty { "https://cdn-icons-png.flaticon.com/512/149/149071.png" },
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.LightGray.copy(0.5f), CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = user.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            Text(text = "@${user.username}", fontSize = 14.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(24.dp))

                            // --- 🤝 FOLLOW BUTTON ---
                            if (targetUserId != currentUserId) {
                                Button(
                                    onClick = {
                                        viewModel.toggleFollow(targetUserId, isFollowing)
                                    },
                                    modifier = Modifier.fillMaxWidth(0.7f).height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowing) Color.LightGray.copy(0.3f) else DeepNavy,
                                        contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.White
                                    )
                                ) {
                                    Text(
                                        text = if (isFollowing) "FOLLOWING" else "FOLLOW",
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                "BROADCAST HISTORY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Start),
                                letterSpacing = 1.5.sp
                            )
                            HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 0.5.dp, color = Color.LightGray.copy(0.4f))
                        }
                    }

                    // --- 📜 BROADCAST LIST ---
                    if (userPosts.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                                Text("No reports shared yet.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        items(userPosts) { post ->
                            // 🌟 Calls shared card from PostComponents.kt
                            UserTimelineCard(
                                post = post,
                                onClick = { onNavigateToPostDetail(post.id) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}