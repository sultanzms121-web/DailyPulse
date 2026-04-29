package com.dailypulse.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.dailypulse.Constants
import com.dailypulse.model.Post
import com.dailypulse.model.User as MyUser
import com.dailypulse.viewmodel.AuthViewModel
import com.dailypulse.viewmodel.HomeViewModel
import com.dailypulse.viewmodel.SocialViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private val DeepNavy = Color(0xFF002147)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    socialViewModel: SocialViewModel,
    onNavigateToUserSearch: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToFollowingList: () -> Unit // 🌟 ADDED: Link to your Following list
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE) }
    val firebaseUser = viewModel.getCurrentUser()

    // --- 📊 UI STATE ---
    var userData by remember { mutableStateOf<MyUser?>(null) }
    val myTimeline = remember { mutableStateListOf<Post>() }
    var isEditUsernameOpen by remember { mutableStateOf(false) }
    var isBroadcastOpen by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var currentTheme by remember { mutableStateOf(sharedPrefs.getString(Constants.KEY_THEME_MODE, "system") ?: "system") }
    var currentLanguage by remember { mutableStateOf(sharedPrefs.getString(Constants.KEY_NEWS_LANGUAGE, "en") ?: "en") }

    // 🌍 EXTENDED GLOBAL LANGUAGE LIST
    val availableLanguages = listOf(
        "en" to "English", "bn" to "বাংলা", "hi" to "हिन्दी", "ur" to "اردو",
        "ar" to "العربية", "es" to "Español", "fr" to "Français", "de" to "Deutsch",
        "ru" to "Русский", "ja" to "日本語", "zh" to "中文", "pt" to "Português",
        "it" to "Italiano", "tr" to "Türkçe", "ko" to "한국어"
    )

    // --- 🚀 LISTENERS ---
    LaunchedEffect(firebaseUser) {
        firebaseUser?.let { user ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid).addSnapshotListener { snapshot, _ ->
                userData = snapshot?.toObject(MyUser::class.java)
            }
            db.collection("posts")
                .whereEqualTo("authorId", user.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        myTimeline.clear()
                        myTimeline.addAll(snapshot.toObjects(Post::class.java))
                    }
                }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.updateProfilePicture(context, it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.ExtraBold) },
                actions = { IconButton(onClick = { showSettingsSheet = true }) { Icon(Icons.Default.Settings, null) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            // 1. HEADER
            item {
                ProfileHeader(
                    photo = firebaseUser?.photoUrl?.toString(),
                    name = firebaseUser?.displayName ?: "Reporter",
                    handle = userData?.username ?: "user",
                    onEdit = { isEditUsernameOpen = true },
                    onChange = { photoPickerLauncher.launch("image/*") }
                )
            }

            // 2. SOCIAL CARDS
            item {
                SocialNetworkCard(
                    followingCount = userData?.following?.size ?: 0,
                    onSearchClick = onNavigateToUserSearch,
                    onFollowingClick = onNavigateToFollowingList // 🌟 FIXED: Clickable following
                )
            }

            // 3. BROADCAST
            item { NewsBroadcastPrompt(firebaseUser?.photoUrl?.toString()) { isBroadcastOpen = true } }

            item {
                Text(
                    "MY BROADCASTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp),
                    letterSpacing = 1.sp
                )
            }

            // 4. TIMELINE
            if (myTimeline.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) { Text("You haven't posted any reports yet.", color = Color.Gray) } }
            } else {
                items(myTimeline) { post ->
                    UserTimelineCard(post = post, onClick = { onNavigateToPostDetail(post.id) })
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    // --- 🎙️ DIALOGS ---
    if (isBroadcastOpen) {
        NewsBroadcastDialog(
            userPhoto = firebaseUser?.photoUrl?.toString(),
            userName = firebaseUser?.displayName ?: "User",
            onDismiss = { isBroadcastOpen = false },
            onPost = { h, d, c ->
                socialViewModel.postNews(firebaseUser?.uid ?: "", firebaseUser?.displayName ?: "User", userData?.username ?: "user", firebaseUser?.photoUrl.toString(), h, d)
                isBroadcastOpen = false
            }
        )
    }

    if (showSettingsSheet) {
        ModalBottomSheet(onDismissRequest = { showSettingsSheet = false }, sheetState = sheetState) {
            SettingsSheetContent(
                currentTheme = currentTheme,
                currentLanguage = currentLanguage,
                availableLanguages = availableLanguages,
                onThemeChange = { mode ->
                    currentTheme = mode
                    sharedPrefs.edit().putString(Constants.KEY_THEME_MODE, mode).apply()
                    showSettingsSheet = false
                },
                onLanguageChange = { code ->
                    currentLanguage = code
                    sharedPrefs.edit().putString(Constants.KEY_NEWS_LANGUAGE, code).apply()
                    homeViewModel.manualRefresh("All")
                },
                onSignOut = { viewModel.logout(); onNavigateToAuth() }
            )
        }
    }

    if (isEditUsernameOpen) {
        var newName by remember { mutableStateOf(userData?.username ?: "") }
        AlertDialog(
            onDismissRequest = { isEditUsernameOpen = false },
            title = { Text("Update Handle") },
            text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Username") }, singleLine = true) },
            confirmButton = {
                Button(onClick = {
                    socialViewModel.updateUsername(firebaseUser?.uid ?: "", newName)
                    isEditUsernameOpen = false
                }) { Text("Update") }
            }
        )
    }
}

// --- 🧱 UI COMPONENTS ---

@Composable
fun ProfileHeader(photo: String?, name: String, handle: String, onEdit: () -> Unit, onChange: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(modifier = Modifier.size(100.dp), shape = CircleShape, border = BorderStroke(2.dp, Color.LightGray.copy(0.5f))) {
                AsyncImage(model = photo, contentDescription = null, contentScale = ContentScale.Crop)
            }
            IconButton(onClick = onChange, modifier = Modifier.size(32.dp).background(DeepNavy, CircleShape).border(2.dp, Color.White, CircleShape)) {
                Icon(Icons.Default.PhotoCamera, null, Modifier.size(16.dp), tint = Color.White)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Row(Modifier.clickable { onEdit() }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("@$handle", color = Color.Gray, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.Edit, null, Modifier.size(14.dp).padding(start = 6.dp), tint = Color.Gray)
        }
    }
}

@Composable
fun SocialNetworkCard(followingCount: Int, onSearchClick: () -> Unit, onFollowingClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
    ) {
        Column(Modifier.padding(8.dp)) {
            ListItem(
                headlineContent = { Text("Find Reporters", fontWeight = FontWeight.Bold) },
                leadingContent = { Icon(Icons.Default.Search, null, tint = DeepNavy) },
                modifier = Modifier.clickable { onSearchClick() }
            )
            ListItem(
                headlineContent = { Text("Following", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("$followingCount contributors") },
                leadingContent = { Icon(Icons.Default.People, null, tint = DeepNavy) },
                modifier = Modifier.clickable { onFollowingClick() } // 🌟 Actionable!
            )
        }
    }
}

@Composable
fun NewsBroadcastPrompt(userPhoto: String?, onPromptClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onPromptClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy.copy(alpha = 0.05f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = userPhoto, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(16.dp))
            Text("Broadcast your report...", color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.RssFeed, null, tint = DeepNavy)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheetContent(
    currentTheme: String,
    currentLanguage: String,
    availableLanguages: List<Pair<String, String>>,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onSignOut: () -> Unit
) {
    Column(Modifier.padding(24.dp).fillMaxWidth().padding(bottom = 40.dp)) {
        Text("App Preferences", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(24.dp))

        Text("Theme Selection", fontWeight = FontWeight.Bold)
        Row(Modifier.padding(top = 12.dp)) {
            listOf("light", "dark", "system").forEach { mode ->
                FilterChip(
                    selected = currentTheme == mode,
                    onClick = { onThemeChange(mode) },
                    label = { Text(mode.uppercase()) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text("News Content Language", fontWeight = FontWeight.Bold)
        // 🌍 Using FlowRow to wrap many languages across multiple lines
        FlowRow(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            availableLanguages.forEach { (code, name) ->
                FilterChip(
                    selected = currentLanguage == code,
                    onClick = { onLanguageChange(code) },
                    label = { Text(name) }
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.1f), contentColor = Color.Red),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("SIGN OUT", fontWeight = FontWeight.ExtraBold)
        }
    }
}