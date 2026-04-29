package com.dailypulse.data.local

import androidx.room.*
import com.dailypulse.model.NewsSource
import kotlinx.coroutines.flow.Flow

/**
 * 🌟 SOURCE DATA ACCESS OBJECT
 * Manages the master list of news providers (e.g., BBC, Rtv, Prothom Alo).
 */
@Dao
interface SourceDao {

    /**
     * Fetches all available news sources stored in the local cache.
     */
    @Query("SELECT * FROM sources ORDER BY name ASC")
    fun getAllSources(): Flow<List<NewsSource>>

    /**
     * 🔍 CATEGORY FILTER:
     * Useful for your specific portals like "International Affairs".
     */
    @Query("SELECT * FROM sources WHERE category = :category")
    fun getSourcesByCategory(category: String): Flow<List<NewsSource>>

    /**
     * 💾 Bulk Insert:
     * Best for the initial app setup or when refreshing the full list of providers.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<NewsSource>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: NewsSource)

    /**
     * Removes a source from the master list.
     */
    @Query("DELETE FROM sources WHERE id = :sourceId")
    suspend fun deleteSource(sourceId: String)

    /**
     * Wipes all source data (useful for a full app reset).
     */
    @Query("DELETE FROM sources")
    suspend fun clearAllSources()
}