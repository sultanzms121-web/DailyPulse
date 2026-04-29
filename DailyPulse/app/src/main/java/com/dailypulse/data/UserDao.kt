package com.dailypulse.data.local

import androidx.room.*
import com.dailypulse.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 🌟 USER DATA ACCESS OBJECT
 * Manages the local profile data for the DailyPulse user.
 */
@Dao
interface UserDao {

    /**
     * 🔍 Fetch the user profile.
     * In 2026 Android development, we return a Flow so the UI
     * automatically updates if the user changes their name or photo.
     */
    @Query("SELECT * FROM users WHERE id= :userId LIMIT 1")
    fun getUserById(userId: String): Flow<User?>

    /**
     * 💾 Save or Update the user's profile.
     * This will handle both initial setup and future profile edits.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * 🧹 Clear User Data
     * Essential for logout functionality to ensure data privacy.
     */
    @Query("DELETE FROM users")
    suspend fun clearUserData()
}