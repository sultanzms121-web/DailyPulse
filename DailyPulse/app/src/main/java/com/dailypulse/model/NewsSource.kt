package com.dailypulse.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class NewsSource(
    @PrimaryKey val id: String,
    val name: String,
    val rssFeedUrl: String,
    val category: String,
    val isActive: Boolean = true,
    val articlesCount: Int = 0,
    val avgEngagementScore: Float = 0f
)
