package com.dailypulse.data

import android.util.Log
import com.dailypulse.Constants
import com.dailypulse.data.local.ArticleDao
import com.dailypulse.data.local.FollowedSource
import com.dailypulse.data.remote.GeminiManager
import com.dailypulse.data.remote.RetrofitClient
import com.dailypulse.data.remote.toEntity
import com.dailypulse.model.NewsArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.net.URLEncoder
import kotlin.math.abs

/**
 * 🌟 THE DATA HUB
 * Manages the flow between the NewsData.io API, Room Database, and Gemini AI.
 */
class NewsRepository(
    private val articleDao: ArticleDao,
    private val geminiManager: GeminiManager = GeminiManager()
) {

    // --- Local Data Flows (Instant UI Updates) ---

    fun getFollowedStories(): Flow<List<NewsArticle>> = articleDao.getFollowedStories()

    fun getArticlesBySource(sourceId: String): Flow<List<NewsArticle>> =
        articleDao.getArticlesBySource(sourceId)

    fun isFollowing(sourceId: String): Flow<Boolean> = articleDao.isFollowing(sourceId)

    fun getArticles(category: String, language: String): Flow<List<NewsArticle>> =
        articleDao.getPrioritizedArticles(category, language)

    fun getBookmarkedArticles(): Flow<List<NewsArticle>> = articleDao.getBookmarkedArticles()

    // --- Source Management ---

    suspend fun followSource(sourceId: String, sourceName: String) {
        val source = FollowedSource(sourceId = sourceId, sourceName = sourceName)
        articleDao.followSource(source)
    }

    suspend fun unfollowSource(sourceId: String) = articleDao.unfollowSource(sourceId)

    // --- Network Logic (Retrofit) ---

    suspend fun refreshNews(
        category: String,
        language: String,
        page: String? = null
    ): String? {
        return try {
            val cleanLanguage = language.lowercase().take(2)
            val apiCategory = mapCategory(category)

            val query = if (apiCategory == "top") {
                Constants.DEFAULT_QUERY
            } else if (category == "International Affairs") {
                "geopolitics diplomacy global"
            } else category

            val categoryParam = if (apiCategory == "top") "top" else null

            // API Key is automatically injected by the OkHttp Interceptor
            val response = RetrofitClient.newsApiService.getNews(
                query = query,
                category = categoryParam,
                language = cleanLanguage,
                page = page.takeIf { it?.isNotBlank() == true }
            )

            if (response.results.isNotEmpty()) {
                val bookmarkedUrls = articleDao.getBookmarkedArticles().firstOrNull()?.map { it.contentUrl } ?: emptyList()
                val readHistoryUrls = articleDao.getReadHistory().firstOrNull()?.map { it.contentUrl } ?: emptyList()

                val articles = response.results.map { remote ->
                    remote.toEntity(category = category, language = cleanLanguage).copy(
                        isBookmarked = bookmarkedUrls.contains(remote.link),
                        isRead = readHistoryUrls.contains(remote.link)
                    )
                }

                if (page == null) {
                    articleDao.deleteStaleLanguageArticles(category, cleanLanguage)
                }

                articleDao.insertArticles(articles)
                response.nextPage
            } else null
        } catch (e: Exception) {
            Log.e("NewsRepository", "Refresh Failed: ${e.localizedMessage}")
            null
        }
    }

    // --- AI Enrichment (Gemini 3 Flash & Pollinations) ---

    /**
     * Fetches or generates a visual prompt for the article thumbnail.
     */
    suspend fun getEnrichedImageUrl(article: NewsArticle): String {
        if (!article.imageUrl.isNullOrBlank()) return article.imageUrl

        return try {
            // GeminiManager already handles '+' replacement for URL safety
            val rawPrompt = geminiManager.generateVisualDescription(article.title)
            val encodedPrompt = URLEncoder.encode(rawPrompt, "UTF-8")
            val seed = abs(article.contentUrl.hashCode())

            // Using FLUX model for higher quality cinematic news visuals
            "https://image.pollinations.ai/prompt/$encodedPrompt?width=1080&height=720&nologo=true&seed=$seed&model=flux"
        } catch (e: Exception) {
            "https://images.unsplash.com/photo-1504711432869-efd597cdd042" // Fallback News Image
        }
    }

    /**
     * Generates a 3-bullet summary in the correct language.
     * Uses 'summary' as fallback if the AI is unavailable.
     */
    suspend fun getEnrichedAiSummary(article: NewsArticle, languageCode: String): String {
        // If we already have a generated summary, return it immediately
        if (!article.aiSummary.isNullOrBlank()) return article.aiSummary

        val languageName = when (languageCode.lowercase().take(2)) {
            "bn" -> "Bengali"
            "hi" -> "Hindi"
            else -> "English"
        }

        // Aligning with your updated NewsArticle entity field names
        val textToSummarize = article.summary.ifBlank { article.title }

        return try {
            val generatedSummary = geminiManager.generateSummary(
                title = article.title,
                description = textToSummarize,
                language = languageName,
                category = article.category
            )

            if (!generatedSummary.isNullOrBlank()) {
                articleDao.updateAiSummary(article.contentUrl, generatedSummary)
                generatedSummary
            } else {
                article.summary
            }
        } catch (e: Exception) {
            Log.e("NewsRepository", "Gemini Failed: ${e.message}")
            article.summary
        }
    }

    // --- Search & Utilities ---

    suspend fun refreshSearchNews(query: String, language: String, page: String? = null): String? {
        if (query.isBlank()) return null
        return try {
            val cleanLanguage = language.lowercase().take(2)
            val response = RetrofitClient.newsApiService.searchNews(
                query = query,
                language = cleanLanguage,
                page = page.takeIf { it?.isNotBlank() == true }
            )

            if (response.results.isNotEmpty()) {
                val bookmarkedUrls = articleDao.getBookmarkedArticles().firstOrNull()?.map { it.contentUrl } ?: emptyList()
                val searchEntities = response.results.map { remote ->
                    remote.toEntity("Search", cleanLanguage).copy(isBookmarked = bookmarkedUrls.contains(remote.link))
                }
                if (page == null) articleDao.clearSearch()
                articleDao.insertArticles(searchEntities)
                response.nextPage
            } else null
        } catch (e: Exception) { null }
    }

    private fun mapCategory(category: String): String? = when (category) {
        "Trending", "For you", "All" -> "top"
        "International Affairs" -> "politics"
        "Technology" -> "technology"
        "Sports" -> "sports"
        "Business" -> "business"
        "Health" -> "health"
        "Science" -> "science"
        "Entertainment" -> "entertainment"
        else -> null
    }

    suspend fun toggleBookmark(article: NewsArticle) = articleDao.updateBookmarkStatus(article.contentUrl, !article.isBookmarked)
    suspend fun markAsRead(url: String) = articleDao.markAsRead(url)
    suspend fun clearSearch() = articleDao.clearSearch()
}