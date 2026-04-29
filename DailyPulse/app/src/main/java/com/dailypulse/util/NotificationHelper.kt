package com.dailypulse.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dailypulse.MainActivity

class NotificationHelper(private val context: Context) {

    private val channelId = "breaking_news_channel"
    private val notificationId = 101

    /**
     * Builds and displays the notification.
     * @param url The deep link URL that will be passed to MainActivity when clicked.
     */
    fun showNotification(title: String, message: String, url: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Create the Notification Channel (Required for API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Breaking News"
            val descriptionText = "Notifications for the latest global headlines"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Create the Intent for Deep Linking
        val intent = Intent(context, MainActivity::class.java).apply {
            // Ensure the activity restarts or handles the new intent properly
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("article_url", url)
        }

        // 3. Wrap intent in a PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Build the Notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon (e.g. R.drawable.ic_news)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Expansion support
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Removes notification when clicked
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        // 5. Trigger the notification
        notificationManager.notify(notificationId, builder.build())
    }
}