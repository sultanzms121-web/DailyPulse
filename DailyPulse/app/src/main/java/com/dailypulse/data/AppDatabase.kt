package com.dailypulse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dailypulse.model.NewsArticle
import com.dailypulse.model.NewsSource
import com.dailypulse.model.User
import com.dailypulse.data.local.ArticleDao
import com.dailypulse.data.local.UserDao
import com.dailypulse.data.local.SourceDao
import com.dailypulse.data.local.FollowedSource
import com.dailypulse.util.Converters

/**
 * 🌟 THE DATABASE HUB
 * version 3: Added support for AI summaries and Followed Sources.
 */
@Database(
    entities = [
        User::class,
        NewsArticle::class,
        NewsSource::class,
        FollowedSource::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun articleDao(): ArticleDao
    abstract fun sourceDao(): SourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "DailyPulse_db"
                )
                    /**
                     * ⚠️ WARNING: In development, this wipes old data to apply
                     * the new version 3 schema. Perfect for our refactoring stage.
                     */
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}