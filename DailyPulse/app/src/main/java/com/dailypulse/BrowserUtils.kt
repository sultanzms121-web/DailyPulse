package com.dailypulse.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * This helper function opens a URL in a professional "Custom Tab"
 * which keeps the user inside the DailyPulse experience.
 */
fun openNewsArticle(context: Context, url: String) {
    if (url.isEmpty()) return // Safety first!

    try {
        val builder = CustomTabsIntent.Builder()

        // Optional: Customizing the toolbar color to match your app
        builder.setShowTitle(true)

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        // If they don't have a browser (rare), this prevents a crash
        e.printStackTrace()
    }
}