package com.dailypulse.data // Adjust this to match your package structure

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    /**
     * 1. Register a new user with Email and Password.
     * Firebase automatically handles the password hashing.
     */
    suspend fun registerWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 2. Log in an existing user with Email and Password.
     */
    suspend fun loginWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 3. Exchange a Google ID Token for a Firebase Credential.
     * This is what makes the "Continue with Google" button work.
     */
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 4. Check if a user is currently logged in.
     * Useful for skipping the Login screen on app startup.
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    /**
     * 5. Log out the user.
     */
    fun logout() {
        firebaseAuth.signOut()
    }
}