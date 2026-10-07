package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.model.ClubhouseFirestoreEvent

/**
 * EventNotificationScheduler
 * Schedules local alarms and reminders for upcoming events fetched from Cloud Firestore.
 */
class EventNotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules a local notification for an upcoming clubhouse event.
     * @param event The event from Firestore
     * @param delayMillis Delay before the reminder fires (defaults to 15 seconds for immediate test demo, or trigger timestamp)
     */
    fun scheduleEventReminder(event: ClubhouseFirestoreEvent, delayMillis: Long = 15_000L) {
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra(EventReminderReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(EventReminderReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(EventReminderReceiver.EXTRA_EVENT_LOCATION, event.location)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + delayMillis

        try {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("NotificationScheduler", "Scheduled reminder for ${event.title} in ${delayMillis / 1000}s")
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Failed to schedule alarm", e)
        }
    }

    /**
     * Cancels an existing scheduled event reminder.
     */
    fun cancelEventReminder(eventId: String) {
        val intent = Intent(context, EventReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
