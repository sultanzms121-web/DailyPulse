package com.dailypulse.data.remote

import com.dailypulse.model.NewsArticle
import com.dailypulse.model.NewsArticleRemote

/**
 * 🌟 THE MAPPER
 * Converts raw API data (NewsArticleRemote) into our local Room Entity (NewsArticle).
 * This is where we handle default values and format data for the database.
 */
fun NewsArticleRemote.toEntity(category: String, language: String): NewsArticle {
    return NewsArticle(
        title = this.title,
        sourceId = this.sourceId,
        // Using 'source_id' as a fallback for 'sourceName' if the API is sparse
        sourceName = this.sourceId ?: "Unknown Source",
        pubDate = this.pubDate ?: "Just now",
        author = null, // Can be mapped if the API provides it
        summary = this.description ?: "No description available",
        aiSummary = null, // Always starts as null until Gemini processes it
        contentUrl = this.link,
        imageUrl = this.imageUrl,
        category = category,
        language = language,
        readTimeMinutes = 5, // Default estimation
        isBookmarked = false,
        isRead = false
    )
}