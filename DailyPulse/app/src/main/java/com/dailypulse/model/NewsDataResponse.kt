package com.dailypulse.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewsDataResponse(
    val status: String,
    val totalResults: Int,
    val results: List<NewsArticleRemote>,
    val nextPage: String? = null
)

@JsonClass(generateAdapter = true)
data class NewsArticleRemote(
    val title: String,
    val link: String,
    val description: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "source_id") val sourceId: String? = null,
    val pubDate: String? = null,
    val content: String? = null,
    val country: List<String>? = null,
    val category: List<String>? = null,
    val language: String? = null
)