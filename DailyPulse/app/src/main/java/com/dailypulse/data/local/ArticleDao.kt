package com.dailypulse.data.local

import androidx.room.*
import com.dailypulse.model.NewsArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>)

    @Query("SELECT * FROM articles WHERE category = :category AND language = :language ORDER BY pubDate DESC")
    fun getPrioritizedArticles(category: String, language: String): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followSource(source: FollowedSource)

    @Query("DELETE FROM followed_sources WHERE sourceId = :sourceId")
    suspend fun unfollowSource(sourceId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM followed_sources WHERE sourceId = :sourceId)")
    fun isFollowing(sourceId: String): Flow<Boolean>

    @Query("SELECT * FROM articles WHERE sourceId IN (SELECT sourceId FROM followed_sources) LIMIT 20")
    fun getFollowedStories(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE sourceId = :sourceId")
    fun getArticlesBySource(sourceId: String): Flow<List<NewsArticle>>

    @Query("UPDATE articles SET aiSummary = :summary WHERE contentUrl = :url")
    suspend fun updateAiSummary(url: String, summary: String)

    @Query("DELETE FROM articles WHERE category = :category AND language = :language AND isBookmarked = 0")
    suspend fun deleteStaleLanguageArticles(category: String, language: String)

    @Query("SELECT * FROM articles WHERE isBookmarked = 1")
    fun getBookmarkedArticles(): Flow<List<NewsArticle>>

    @Query("UPDATE articles SET isBookmarked = :status WHERE contentUrl = :url")
    suspend fun updateBookmarkStatus(url: String, status: Boolean)

    @Query("SELECT * FROM articles WHERE isRead = 1")
    fun getReadHistory(): Flow<List<NewsArticle>>

    @Query("UPDATE articles SET isRead = 1 WHERE contentUrl = :url")
    suspend fun markAsRead(url: String)

    @Query("DELETE FROM articles WHERE category = 'Search'")
    suspend fun clearSearch()
}