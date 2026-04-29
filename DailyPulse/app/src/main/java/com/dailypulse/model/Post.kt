package com.dailypulse.model

// com.dailypulse.model.Post.kt
data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorPhoto: String = "",
    val title: String = "",
    val content: String = "",
    // You can keep these as null so your existing database entries don't crash the app
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val timestamp: Long = 0L
)