package com.dailypulse.model

import androidx.room.Entity
import androidx.room.Index
import java.io.Serializable

/**
 * 🌟 THE CORE ENTITY: NewsArticle
 * * This model represents a news article in the local Room database.
 * Using a composite primary key ensures that the same URL can exist
 * across different categories or languages without data conflicts.
 */
@Entity(
    tableName = "articles",
    primaryKeys = ["contentUrl", "category", "language"],
    indices = [Index(value = ["category", "language"])] // 🚀 Optimization for feed queries
)
data class NewsArticle(
    val title: String,
    val sourceId: String? = null,    // Links to source-following logic
    val sourceName: String,
    val pubDate: String,
    val author: String? = null,
    val summary: String,            // Fallback content from API
    val aiSummary: String? = null,  // Stores the Gemini 3 Flash bullet points
    val contentUrl: String,
    val imageUrl: String? = null,
    val category: String,           // e.g., "International Affairs", "Technology"
    val language: String,           // e.g., "en", "bn", "hi"
    val readTimeMinutes: Int = 5,
    val isBookmarked: Boolean = false,
    val isRead: Boolean = false
) : Serializable // Allows passing the article between Compose screens