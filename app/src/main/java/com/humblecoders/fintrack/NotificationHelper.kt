package com.humblecoders.fintrack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "budgetbae_balance_notifications"
    private const val CHANNEL_NAME = "Balance Notifications"
    
    private val lowBalanceThresholds = mapOf(
        1000.0 to Pair("Balance Alert ⚠️", "You've dipped below 1000. Watch your spending!"),
        500.0 to Pair("Tightening Up 📉", "Only 500 left. Time to budget carefully."),
        200.0 to Pair("Danger Zone 🚨", "Critical! Less than 200 remaining.")
    )
    
    private val highBalanceThresholds = mapOf(
        500.0 to Pair("On the Rise 📈", "You're back above 500. Keep it up!"),
        1500.0 to Pair("Looking Good 🌿", "Balance crossed 1500. Healthy finances!"),
        2000.0 to Pair("Funding Secured 💰", "Over 2000 in the bank! You're crushing it.")
    )
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for balance changes and alerts"
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun checkAndShowBalanceNotifications(
        context: Context,
        currentBalance: Double,
        previousBalance: Double
    ) {
        android.util.Log.d("NotificationHelper", "Checking balance: previous=$previousBalance, current=$currentBalance")
        
        // Check low balance thresholds (when balance decreases below threshold)
        lowBalanceThresholds.forEach { (threshold, notification) ->
            val (title, body) = notification
            if (previousBalance >= threshold && currentBalance < threshold) {
                android.util.Log.d("NotificationHelper", "Low balance threshold crossed: $threshold")
                showNotification(context, title, body, threshold.toInt())
            }
        }
        
        // Check high balance thresholds (when balance increases above threshold)
        highBalanceThresholds.forEach { (threshold, notification) ->
            val (title, body) = notification
            if (previousBalance < threshold && currentBalance >= threshold) {
                android.util.Log.d("NotificationHelper", "High balance threshold crossed: $threshold")
                showNotification(context, title, body, (threshold.toInt() + 10000)) // Offset to avoid ID conflicts
            }
        }
    }
    
    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {
        android.util.Log.d("NotificationHelper", "Showing notification: $title - $message")
        
        // Check if notifications are enabled (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = NotificationManagerCompat.from(context)
            if (!notificationManager.areNotificationsEnabled()) {
                android.util.Log.e("NotificationHelper", "Notifications are not enabled for this app!")
                return
            }
        }
        
        // Get app icon
        val appIcon = try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            applicationInfo.icon
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Error getting app icon: ${e.message}")
            android.R.drawable.sym_def_app_icon // Fallback icon
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(appIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        )
        
        if (notificationManager != null) {
            try {
                notificationManager.notify(notificationId, notification)
                android.util.Log.d("NotificationHelper", "Notification sent successfully with ID: $notificationId")
            } catch (e: Exception) {
                android.util.Log.e("NotificationHelper", "Error showing notification: ${e.message}", e)
            }
        } else {
            android.util.Log.e("NotificationHelper", "NotificationManager is null!")
        }
    }
    
    // Test function to verify notifications work
    fun testNotification(context: Context) {
        showNotification(
            context = context,
            title = "Test Notification 🧪",
            message = "If you see this, notifications are working!",
            notificationId = 99999
        )
    }
}

