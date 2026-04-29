package com.dailypulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit
) {
    // Dummy state for demonstration
    val sources = remember { mutableStateListOf(
        "BBC News", "CNN", "TechCrunch", "The Verge"
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = { /* Add Source */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Source")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Manage News Sources",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sources) { source ->
                    ListItem(
                        headlineContent = { Text(source) },
                        supportingContent = { Text("Active") },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = true, onCheckedChange = { /* Toggle */ })
                                IconButton(onClick = { sources.remove(source) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
