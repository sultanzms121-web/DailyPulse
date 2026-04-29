package com.dailypulse.data.remote

import com.squareup.moshi.Json
import com.dailypulse.model.NewsArticle

data class NewsDataResponse(
    @Json(name = "status") val status: String,
    @Json(name = "totalResults") val totalResults: Int,
    @Json(name = "results") val results: List<RemoteArticle>,
    @Json(name = "nextPage") val nextPage: String?
)

data class RemoteArticle(
    @Json(name = "article_id") val articleId: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?,
    @Json(name = "link") val link: String,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "pubDate") val pubDate: String?,
    @Json(name = "source_id") val sourceId: String?,
    @Json(name = "creator") val creator: List<String>?,
    @Json(name = "content") val content: String?
)

/**
 * 🌟 THE FIX: Added 'language' parameter to the function signature.
 */
fun RemoteArticle.toEntity(category: String, language: String): NewsArticle {
    return NewsArticle(
        title = this.title,
        sourceName = this.sourceId?.replaceFirstChar { it.uppercase() } ?: "General News",
        pubDate = this.pubDate ?: "",
        author = this.creator?.firstOrNull() ?: (this.sourceId ?: "Staff Writer"),
        summary = this.description ?: this.content?.take(200) ?: "No description available",
        contentUrl = this.link,
        imageUrl = this.imageUrl,

        // 🌟 THE SYNC FIX: Assigning the category and language
        category = category,
        language = language,

        readTimeMinutes = ((this.content?.length ?: 500) / 1000) + 1,
        isBookmarked = false,
        isRead = false
    )
}