package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: return
        val eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "Clubhouse Meetup"
        val eventLocation = intent.getStringExtra(EXTRA_EVENT_LOCATION) ?: "Mallu Gang Clubhouse"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "clubhouse_events_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Clubhouse Events & Meetups",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled gaming events and gang trips"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAVIGATE_TO", "events")
            putExtra("EXTRA_EVENT_ID", eventId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🌴 Reminder: $eventTitle")
            .setContentText("Happening soon at $eventLocation! Tap to open.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Get ready for $eventTitle at $eventLocation. Your gang brothers and sisters are gathering!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(eventId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        const val EXTRA_EVENT_TITLE = "EXTRA_EVENT_TITLE"
        const val EXTRA_EVENT_LOCATION = "EXTRA_EVENT_LOCATION"
    }
}
