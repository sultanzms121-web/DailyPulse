package com.dailypulse.data

import androidx.room.*
import com.dailypulse.model.NewsArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    /**
     * 🌟 THE STORIES QUERY
     * Grabs the latest single article from every source you follow.
     */
    @Query("""
        SELECT * FROM articles 
        WHERE sourceId IN (SELECT sourceId FROM followed_sources) 
        GROUP BY sourceId 
        ORDER BY pubDate DESC
    """)
    fun getFollowedStories(): Flow<List<NewsArticle>>

    /**
     * 🌟 THE PRIORITIZED FEED
     * Puts followed sources at the top (Priority 1) and others below (Priority 2).
     */
    @Query("""
        SELECT * FROM articles 
        WHERE (LOWER(category) = LOWER(:category) OR :category = 'All') 
        AND language = :language 
        ORDER BY 
            CASE WHEN sourceId IN (SELECT sourceId FROM followed_sources) THEN 1 ELSE 2 END, 
            pubDate DESC
    """)
    fun getPrioritizedArticles(category: String, language: String): Flow<List<NewsArticle>>

    /**
     * 🌟 NEW: SOURCE PROFILE QUERY
     * Fetches all articles belonging to a specific news channel.
     */
    @Query("SELECT * FROM articles WHERE sourceId = :sourceId ORDER BY pubDate DESC")
    fun getArticlesBySource(sourceId: String): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE category = :category AND language = :language ORDER BY pubDate DESC")
    fun getArticlesByLanguage(category: String, language: String): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 GROUP BY contentUrl ORDER BY pubDate DESC")
    fun getBookmarkedArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE isRead = 1 GROUP BY contentUrl ORDER BY pubDate DESC")
    fun getReadHistory(): Flow<List<NewsArticle>>

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE contentUrl = :url")
    suspend fun updateBookmarkStatus(url: String, isBookmarked: Boolean)

    @Query("UPDATE articles SET isRead = 1 WHERE contentUrl = :url")
    suspend fun markAsRead(url: String)

    @Query("UPDATE articles SET aiSummary = :aiSummary WHERE contentUrl = :url")
    suspend fun updateAiSummary(url: String, aiSummary: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>)

    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' AND language = :language")
    fun searchArticles(query: String, language: String): Flow<List<NewsArticle>>

    @Query("DELETE FROM articles WHERE category = :category AND language != :language AND isBookmarked = 0")
    suspend fun deleteStaleLanguageArticles(category: String, language: String)

    @Query("DELETE FROM articles WHERE category = 'Search'")
    suspend fun clearSearch()

    // 🌟 FOLLOW HELPERS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followSource(source: FollowedSource)

    @Query("DELETE FROM followed_sources WHERE sourceId = :id")
    suspend fun unfollowSource(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM followed_sources WHERE sourceId = :id)")
    fun isFollowing(id: String): Flow<Boolean>
}