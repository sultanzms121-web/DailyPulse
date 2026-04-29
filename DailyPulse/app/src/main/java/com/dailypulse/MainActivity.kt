package com.dailypulse

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.dailypulse.data.AppDatabase
import com.dailypulse.data.NewsRepository
import com.dailypulse.data.worker.NewsWorker
import com.dailypulse.ui.navigation.AppNavigation
import com.dailypulse.ui.navigation.Screen
import com.dailypulse.ui.theme.DailyPulseTheme
import com.dailypulse.util.openNewsArticle
import com.dailypulse.viewmodel.* import java.util.concurrent.TimeUnit

private val SilverBackground = Color(0xFFF5F5F7)

class MainActivity : ComponentActivity() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var socialViewModel: SocialViewModel // 🌟 Added SocialViewModel
    private lateinit var sharedPrefs: SharedPreferences

    private var themeMode by mutableStateOf("system")

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == "theme_mode") {
            themeMode = prefs.getString("theme_mode", "system") ?: "system"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        sharedPrefs = getSharedPreferences("daily_pulse_prefs", Context.MODE_PRIVATE)
        themeMode = sharedPrefs.getString("theme_mode", "system") ?: "system"
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener)

        val database = AppDatabase.getDatabase(applicationContext)
        val newsRepository = NewsRepository(database.articleDao())

        // --- 🏗️ VIEWMODEL INITIALIZATION ---
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        socialViewModel = ViewModelProvider(this)[SocialViewModel::class.java] // 🌟 Initialize SocialViewModel
        homeViewModel = ViewModelProvider(this, HomeViewModelFactory(newsRepository, sharedPrefs))[HomeViewModel::class.java]
        searchViewModel = ViewModelProvider(this, SearchViewModelFactory(newsRepository, sharedPrefs))[SearchViewModel::class.java]

        checkNotificationPermission()
        scheduleBreakingNewsWorker()
        handleNotificationClick(intent)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Reload user data on app launch for persistence
            LaunchedEffect(Unit) {
                authViewModel.reloadUser()
            }

            val isOceanScreen = currentRoute in listOf(
                Screen.Splash.route,
                Screen.Login.route,
                Screen.Register.route
            )

            val useDarkTheme = when {
                isOceanScreen -> true
                themeMode == "light" -> false
                themeMode == "dark" -> true
                else -> isSystemInDarkTheme()
            }

            DailyPulseTheme(darkTheme = useDarkTheme) {
                val backgroundColor = if (currentRoute == "onboarding" && !useDarkTheme) {
                    SilverBackground
                } else {
                    MaterialTheme.colorScheme.background
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    // 🌟 UPDATED: Passing socialViewModel and newsRepository
                    AppNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        homeViewModel = homeViewModel,
                        searchViewModel = searchViewModel,
                        socialViewModel = socialViewModel, // 🌟 Now passed correctly
                        newsRepository = newsRepository
                    )
                }
            }
        }
    }

    private fun handleNotificationClick(intent: Intent?) {
        val articleUrl = intent?.getStringExtra("article_url")
        if (!articleUrl.isNullOrEmpty()) {
            openNewsArticle(this, articleUrl)
            if (::homeViewModel.isInitialized) {
                homeViewModel.onArticleClicked(articleUrl)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationClick(intent)
    }

    private fun scheduleBreakingNewsWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val newsWorkRequest = PeriodicWorkRequestBuilder<NewsWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BreakingNewsWork",
            ExistingPeriodicWorkPolicy.KEEP,
            newsWorkRequest
        )
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sharedPrefs.isInitialized) {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        }
    }
}