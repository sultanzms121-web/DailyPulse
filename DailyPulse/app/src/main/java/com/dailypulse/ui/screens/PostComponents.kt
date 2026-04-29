package com.dailypulse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.dailypulse.model.Post
import java.text.SimpleDateFormat
import java.util.*

private val DeepNavy = Color(0xFF002147)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsBroadcastDialog(
    userPhoto: String?,
    userName: String,
    onDismiss: () -> Unit,
    onPost: (String, String, String) -> Unit
) {
    var headline by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    val categories = listOf("All", "International", "Technology", "Sports", "Business", "Health")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                    Text("New Broadcast", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    TextButton(onClick = { onPost(headline, content, selectedCat) }, enabled = headline.isNotBlank() && content.isNotBlank()) {
                        Text("Post", fontWeight = FontWeight.Bold, color = if(headline.isNotBlank()) DeepNavy else Color.Gray)
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(0.4f))

                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = userPhoto, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Text(userName, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            FilterChip(selected = selectedCat == cat, onClick = { selectedCat = cat }, label = { Text(cat) })
                        }
                    }
                    TextField(
                        value = headline, onValueChange = { headline = it },
                        placeholder = { Text("Headline...", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    TextField(
                        value = content, onValueChange = { content = it },
                        placeholder = { Text("Full report details...", fontSize = 18.sp) },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun UserTimelineCard(post: Post, onClick: () -> Unit = {}) {
    val df = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(0.dp), // 🌟 Full width to match news
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // --- HEADER ---
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.authorPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(CircleShape).border(1.dp, Color.LightGray.copy(0.3f), CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(post.authorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Text("@${post.authorUsername}", fontSize = 10.sp, color = Color.Gray)
                }
            }

            // --- 🌟 GRADIENT TITLE BOX (Replaces Image) ---
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp)
                    .background(Brush.verticalGradient(listOf(DeepNavy.copy(0.8f), DeepNavy)))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = post.title, color = Color.White, fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp, textAlign = TextAlign.Center, lineHeight = 30.sp
                )
            }

            // --- CONTENT & FOOTER ---
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = post.content, style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text(df.format(Date(post.timestamp)), fontSize = 11.sp, color = Color.Gray)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onClick) {
                        Text("READ REPORT", fontWeight = FontWeight.ExtraBold, color = DeepNavy)
                    }
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
        }
    }
}