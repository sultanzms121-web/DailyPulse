package com.dailypulse.data

import com.dailypulse.data.local.UserDao // 🌟 Ensure this matches where you put UserDao
import com.dailypulse.model.User
import kotlinx.coroutines.flow.Flow

/**
 * 🌟 USER REPOSITORY
 * Repository for managing User authentication and local profile data.
 * Acts as the single source of truth for the AuthViewModel.
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * 🔍 Observes a specific user from the local database.
     * Changed to 'getUserById' to match the updated UserDao.
     */
    fun getUser(userId: String): Flow<User?> {
        return userDao.getUserById(userId)
    }

    /**
     * 💾 Saves a new or updated user profile to the local database.
     */
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    /**
     * 🧹 Deletes all user data on logout.
     */
    suspend fun clearUser() {
        userDao.clearUserData()
    }
}