package com.dailypulse.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // 🌟 CRITICAL: Fixes the red 'items' error
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dailypulse.model.User
import com.dailypulse.viewmodel.SocialViewModel
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingListScreen(
    currentUserId: String,
    viewModel: SocialViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPublicProfile: (String) -> Unit
) {
    var followingList by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(currentUserId).addSnapshotListener { snapshot, _ ->
            val ids = snapshot?.get("following") as? List<String> ?: emptyList()
            if (ids.isNotEmpty()) {
                db.collection("users").whereIn("uid", ids).get().addOnSuccessListener { docs ->
                    followingList = docs.toObjects(User::class.java)
                    isLoading = false
                }
            } else {
                followingList = emptyList()
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Following", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF002147))
            }
        } else if (followingList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You aren't following anyone yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding) // 🌟 FIXED: Simple padding call
            ) {
                items(followingList) { user ->
                    ListItem(
                        modifier = Modifier.clickable { onNavigateToPublicProfile(user.id) },
                        leadingContent = {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        },
                        headlineContent = { Text(user.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("@${user.username}", fontSize = 12.sp, color = Color.Gray) },
                        trailingContent = {
                            TextButton(onClick = { viewModel.toggleFollow(user.id, true) }) {
                                Text("Unfollow", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(0.3f))
                }
            }
        }
    }
}