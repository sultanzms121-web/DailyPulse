package com.dailypulse.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// --- PROJECT IMPORTS ---
import com.dailypulse.Constants
import com.dailypulse.viewmodel.AuthViewModel
import com.dailypulse.viewmodel.AuthState
import com.dailypulse.ui.components.OceanWaves
import com.dailypulse.R // 🌟 Needed to access your new ic_google

// --- GOOGLE AUTH IMPORTS ---
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// 🎨 Brand Colors (Clean Navy & White)
private val DeepNavyOcean = Color(0xFF002147)
private val ErrorRed = Color(0xFFFF6B6B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onGuestLogin: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    // --- 🌟 GOOGLE SIGN-IN SETUP ---
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Constants.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { token ->
                viewModel.signInWithGoogle(token)
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Auth Failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DeepNavyOcean) {
        Box(modifier = Modifier.fillMaxSize()) {
            OceanWaves()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome Back",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Log in to catch up on the world",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 🌟 UI FIX: Removed Golden borders. Now glows White when focused.
                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = Color.White
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                AnimatedVisibility(
                    visible = authState is AuthState.Error,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val message = (authState as? AuthState.Error)?.message ?: "An error occurred"
                    Text(
                        text = message,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // --- 1. EMAIL LOGIN (Solid White) ---
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = email.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = DeepNavyOcean,
                        disabledContainerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepNavyOcean, strokeWidth = 2.dp)
                    } else {
                        Text("LOGIN", letterSpacing = 1.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 2. GOOGLE LOGIN (With Official Icon & Clean Border) ---
                OutlinedButton(
                    onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    // 🌟 Removed yellow border, using subtle white instead
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 🌟 THE OFFICIAL ICON
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified // Keeps the original G colors
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("CONTINUE WITH GOOGLE", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 3. 🌟 GUEST LOGIN (Professional Outlined Button) ---
                OutlinedButton(
                    onClick = {
                        viewModel.continueAsGuest()
                        onGuestLogin()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    enabled = authState !is AuthState.Loading,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                ) {
                    Text("CONTINUE AS GUEST", letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 4. REGISTER LINK (Clean White) ---
                TextButton(
                    onClick = onNavigateToRegister,
                    enabled = authState !is AuthState.Loading
                ) {
                    Row {
                        Text("New to Daily Pulse? ", color = Color.White.copy(alpha = 0.6f))
                        Text("Register", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}