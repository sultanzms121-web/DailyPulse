package com.dailypulse.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailypulse.model.Post
import com.dailypulse.model.User as MyUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // 🌟 Required for Merge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SocialViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _searchResults = MutableStateFlow<List<MyUser>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // --- 🔍 USER SEARCH ---
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .whereGreaterThanOrEqualTo("username", query.lowercase())
                    .whereLessThanOrEqualTo("username", query.lowercase() + "\uf8ff")
                    .get().await()
                _searchResults.value = snapshot.toObjects(MyUser::class.java)
            } catch (e: Exception) {
                Log.e("SocialVM", "Search failed: ${e.message}")
                _searchResults.value = emptyList()
            }
        }
    }

    // --- 🤝 TOGGLE FOLLOW/UNFOLLOW (Fail-Safe Version) ---
    fun toggleFollow(targetUserId: String, isFollowing: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Prevent users from following themselves
        if (currentUserId == targetUserId) return

        viewModelScope.launch {
            try {
                val currentUserRef = db.collection("users").document(currentUserId)

                // 🌟 Create a map for the update
                val updateData = if (isFollowing) {
                    mapOf("following" to FieldValue.arrayRemove(targetUserId))
                } else {
                    mapOf("following" to FieldValue.arrayUnion(targetUserId))
                }

                // 🌟 Use SET with MERGE instead of UPDATE
                // This creates the document if it doesn't exist yet!
                currentUserRef.set(updateData, SetOptions.merge()).await()

                Log.d("SocialVM", "Successfully updated follow status for: $targetUserId")
            } catch (e: Exception) {
                // If this logs "Permission Denied", check your Firestore Rules!
                Log.e("SocialVM", "Follow/Unfollow Critical Error: ${e.message}")
            }
        }
    }

    // --- ✏️ UPDATE HANDLE ---
    fun updateUsername(userId: String, newUsername: String) {
        if (newUsername.isBlank()) return
        viewModelScope.launch {
            try {
                db.collection("users").document(userId)
                    .update("username", newUsername.lowercase().trim())
                    .await()
                Log.d("SocialVM", "Username updated to: $newUsername")
            } catch (e: Exception) {
                Log.e("SocialVM", "Update failed: ${e.message}")
            }
        }
    }

    // --- 📝 POST NEWS (Headline + Content) ---
    fun postNews(
        userId: String,
        userName: String,
        userHandle: String,
        userPhoto: String,
        title: String,
        content: String
    ) {
        if (title.trim().isBlank() || content.trim().isBlank()) return

        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document()

                val newPost = Post(
                    id = postRef.id,
                    authorId = userId,
                    authorName = userName,
                    authorUsername = userHandle,
                    authorPhoto = userPhoto,
                    title = title.trim(),
                    content = content.trim(),
                    timestamp = System.currentTimeMillis()
                )

                postRef.set(newPost).await()
                Log.d("SocialVM", "Post created with ID: ${postRef.id}")

            } catch (e: Exception) {
                Log.e("SocialVM", "Post failed: ${e.message}")
            }
        }
    }
}