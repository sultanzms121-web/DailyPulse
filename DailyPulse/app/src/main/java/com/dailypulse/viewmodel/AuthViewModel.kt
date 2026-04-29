package com.dailypulse.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.dailypulse.model.User // 🌟 Custom Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun reloadUser() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                user.reload().await()
                _authState.value = AuthState.Success(auth.currentUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Success(auth.currentUser)
            }
        }
    }

    /**
     * 🖼️ PERMANENT PHOTO UPDATE & FIRESTORE SYNC
     */
    fun updateProfilePicture(context: Context, uri: Uri) {
        val user = auth.currentUser ?: return

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val fileName = "profile_${user.uid}.jpg"
                val destFile = File(context.filesDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                val permanentUri = Uri.fromFile(destFile)

                // 1. Update Auth
                val profileUpdates = userProfileChangeRequest { photoUri = permanentUri }
                user.updateProfile(profileUpdates).await()

                // 2. 🌟 Update Firestore so others see the change
                db.collection("users").document(user.uid)
                    .update("photoUrl", permanentUri.toString()).await()

                user.reload().await()
                _authState.value = AuthState.Success(auth.currentUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to save profile picture")
            }
        }
    }

    /**
     * 🌐 GOOGLE SIGN-IN & AUTOMATIC MIRRORING
     */
    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user!!

                // Check if user exists in Firestore
                val userDoc = db.collection("users").document(firebaseUser.uid).get().await()

                if (!userDoc.exists()) {
                    // Create a searchable handle from email if it's a first-time Google login
                    val generatedUsername = firebaseUser.email?.split("@")?.get(0) ?: "user_${firebaseUser.uid.take(5)}"

                    val newUser = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "New User",
                        username = generatedUsername.lowercase(),
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                        following = emptyList() // Start with no follows
                    )
                    db.collection("users").document(firebaseUser.uid).set(newUser).await()
                }

                _authState.value = AuthState.Success(auth.currentUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google Sign-In Failed")
            }
        }
    }

    /**
     * 🔑 EMAIL/PASSWORD LOGIN
     */
    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                _authState.value = AuthState.Success(auth.currentUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login Failed")
            }
        }
    }

    /**
     * ✨ EMAIL/PASSWORD REGISTER & FIRESTORE INITIALIZATION
     */
    fun register(name: String, username: String, email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // 1. Create the Auth account
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = result.user ?: throw Exception("User creation failed")

                // 2. Set Display Name in Auth
                val profileUpdates = userProfileChangeRequest { displayName = name }
                firebaseUser.updateProfile(profileUpdates).await()

                // 3. 🌟 Create the Firestore Public Profile
                val newUser = User(
                    id = firebaseUser.uid,
                    name = name,
                    username = username.lowercase().trim(),
                    email = email,
                    photoUrl = "",
                    following = emptyList()
                )
                db.collection("users").document(firebaseUser.uid).set(newUser).await()

                firebaseUser.reload().await()
                _authState.value = AuthState.Success(auth.currentUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration Failed")
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun continueAsGuest() {
        _authState.value = AuthState.Success(null)
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}