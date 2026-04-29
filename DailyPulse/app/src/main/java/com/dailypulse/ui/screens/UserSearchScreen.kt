package com.dailypulse.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dailypulse.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchScreen(
    viewModel: SocialViewModel,
    onNavigateToProfile: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Find Creators", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.searchUsers(it)
                },
                placeholder = { Text("Search by @username") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            LazyColumn {
                items(results) { user ->
                    ListItem(
                        modifier = Modifier.clickable { onNavigateToProfile(user.id) },
                        headlineContent = { Text(user.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("@${user.username}") },
                        leadingContent = {
                            AsyncImage(
                                model = user.photoUrl.ifEmpty { "https://cdn-icons-png.flaticon.com/512/149/149071.png" },
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    )
                }
            }
        }
    }
}