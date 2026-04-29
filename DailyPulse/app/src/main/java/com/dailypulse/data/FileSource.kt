package com.dailypulse.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 🌟 THE FOLLOWED SOURCE ENTITY
 * This table stores the unique ID and Name of every news channel
 * the user clicks "Follow" on.
 */
@Entity(tableName = "followed_sources")
data class FollowedSource(
    @PrimaryKey
    val sourceId: String, // e.g., "bbc-news", "rtv-news"
    val sourceName: String
)