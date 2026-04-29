package com.dailypulse.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dailypulse.Constants
import com.dailypulse.data.remote.RetrofitClient
import com.dailypulse.util.NotificationHelper

/**
 * 🌟 BACKGROUND NEWS SYNC
 * Fetches the latest headline in the background and notifies the user.
 */
class NewsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sharedPrefs = applicationContext.getSharedPreferences("news_prefs", Context.MODE_PRIVATE)
        val notificationHelper = NotificationHelper(applicationContext)

        val areNotificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
        val lastNotifiedUrl = sharedPrefs.getString("last_notified_url", "")

        // 1. Check user preferences
        if (!areNotificationsEnabled) return Result.success()

        return try {
            // 2. SANITIZE LANGUAGE
            // NewsData.io rejects long codes (e.g., "en-US") with 422 errors.
            val rawLanguage = sharedPrefs.getString("news_language", "en") ?: "en"
            val cleanLanguage = rawLanguage.lowercase().take(2)

            // 🌟 3. CLEAN API CALL
            // 'apiKey' is GONE. It is now handled by the Interceptor in RetrofitClient.
            val response = RetrofitClient.newsApiService.getNews(
                language = cleanLanguage,
                category = null,
                query = Constants.DEFAULT_QUERY
            )

            // 4. DATA ACCESS
            // Accessing the first result from the 2026 NewsDataResponse model
            val latestArticle = response.results.firstOrNull()

            if (latestArticle != null && latestArticle.link != lastNotifiedUrl) {

                notificationHelper.showNotification(
                    title = "Breaking: ${latestArticle.sourceId?.uppercase() ?: "DailyPulse"}",
                    message = latestArticle.title,
                    url = latestArticle.link
                )

                // Save the link to avoid double-notifying the user
                sharedPrefs.edit()
                    .putString("last_notified_url", latestArticle.link)
                    .apply()
            }

            Log.d("NewsWorker", "Background fetch successful")
            Result.success()

        } catch (e: Exception) {
            Log.e("NewsWorker", "Background fetch failed: ${e.localizedMessage}")

            // 5. SMART RETRY LOGIC
            // If it's a security (401) or validation (422) error, retrying won't fix it.
            val errorMsg = e.message ?: ""
            if (errorMsg.contains("401") || errorMsg.contains("422")) {
                return Result.failure()
            }

            // For network timeouts or server lag, tell WorkManager to try again later.
            Result.retry()
        }
    }
}