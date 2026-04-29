package com.dailypulse.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followed_sources")
data class FollowedSource(
    @PrimaryKey val sourceId: String,
    val sourceName: String
)