package com.dailypulse.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "", // This will be the Firebase UID
    val name: String = "",           // Display Name
    val username: String = "",       // 🌟 NEW: For searching (e.g., @zarif)
    val email: String = "",
    val photoUrl: String = "",       // 🌟 NEW: For the profile picture
    val following: List<String> = emptyList(), // 🌟 NEW: UIDs of people you follow
    val isGuest: Boolean = false,
    val preferredCategories: List<String> = emptyList(),
    val isAdmin: Boolean = false
)