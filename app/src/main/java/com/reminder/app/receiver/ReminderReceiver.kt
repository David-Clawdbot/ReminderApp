package com.reminder.app.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.reminder.app.R
import com.reminder.app.ui.MainActivity

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "ReminderReceiver"
        private const val CHANNEL_ID = "reminder_channel"
        private const val CHANNEL_NAME = "提醒通知"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "=== ReminderReceiver onReceive ===")
        Log.i(TAG, "Action: ${intent.action}")

        if (intent.action != "com.reminder.app.ACTION_REMINDER") return

        val id = intent.getLongExtra("reminder_id", -1)
        val title = intent.getStringExtra("reminder_title") ?: "提醒"
        val content = intent.getStringExtra("reminder_content") ?: ""

        Log.i(TAG, "Reminder triggered: id=$id, title=$title")

        createNotificationChannel(context)
        showNotification(context, id, title, content)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "定时提醒通知"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, id: Long, title: String, content: String) {
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id.toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content.ifEmpty { "到时间了！" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id.toInt(), notification)
        Log.i(TAG, "Notification shown for: $title")
    }
}
