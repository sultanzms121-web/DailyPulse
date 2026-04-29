package com.dailypulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailypulse.model.Post
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(postId: String, onBack: () -> Unit) {
    var post by remember { mutableStateOf<Post?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch the specific post from Firestore
    LaunchedEffect(postId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                post = document.toObject(Post::class.java)
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF002147))
            } else if (post != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 🌟 Headline (Title)
                    Text(
                        text = post!!.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 32.sp,
                        color = Color(0xFF002147)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Author Info
                    Text(
                        text = "By ${post!!.authorName} (@${post!!.authorUsername})",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

                    // 🌟 Full Report (Content)
                    Text(
                        text = post!!.content,
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    )
                }
            } else {
                Text(
                    "Report not found.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            }
        }
    }
}