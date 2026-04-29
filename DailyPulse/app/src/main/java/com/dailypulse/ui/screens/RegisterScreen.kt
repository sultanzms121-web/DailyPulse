package com.dailypulse.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// --- PROJECT IMPORTS ---
import com.dailypulse.viewmodel.AuthViewModel
import com.dailypulse.viewmodel.AuthState
import com.dailypulse.ui.components.OceanWaves

// --- COMPOSE STATE ---
import androidx.compose.runtime.collectAsState

// 🎨 Brand Colors
private val DeepNavyOcean = Color(0xFF002147)
private val ErrorRed = Color(0xFFFF6B6B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") } // 🌟 NEW STATE
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.authState.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.resetState() }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DeepNavyOcean) {
        Box(modifier = Modifier.fillMaxSize()) {
            OceanWaves()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Join Daily Pulse to personalize your news",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = Color.White
                )

                // 1. Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; localError = null },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 🌟 2. Username Field (NEW)
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        // Automatically format for social handle standards
                        username = it.lowercase().trim().replace(" ", "_")
                        localError = null
                    },
                    label = { Text("Username") },
                    placeholder = { Text("e.g. dev_pro", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; localError = null },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; localError = null },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; localError = null },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = textFieldColors,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                // Smooth Error Display
                val displayError = localError ?: (authState as? AuthState.Error)?.message
                AnimatedVisibility(
                    visible = displayError != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = displayError ?: "",
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // --- REGISTER BUTTON ---
                Button(
                    onClick = {
                        when {
                            name.isBlank() -> localError = "Please enter your full name"
                            username.length < 3 -> localError = "Username is too short"
                            password != confirmPassword -> localError = "Passwords do not match"
                            password.length < 6 -> localError = "Password must be at least 6 characters"
                            else -> viewModel.register(name, username, email, password) // 🌟 Passed username
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = name.isNotBlank() && username.isNotBlank() && email.isNotBlank() &&
                            password.isNotBlank() && authState !is AuthState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = DeepNavyOcean,
                        disabledContainerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DeepNavyOcean,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("REGISTER", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Row {
                        Text("Already a member? ", color = Color.White.copy(alpha = 0.6f))
                        Text("Login", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}