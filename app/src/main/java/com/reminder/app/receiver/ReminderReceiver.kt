package com.reminder.app.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.reminder.app.R
import com.reminder.app.data.Reminder
import com.reminder.app.data.ReminderData
import com.reminder.app.data.RepeatMode
import com.reminder.app.service.AlarmScheduler
import com.reminder.app.ui.MainActivity
import com.reminder.app.ui.ReminderAlarmActivity

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1)
        val isPreNotify = intent.getBooleanExtra("is_pre_notify", false)

        if (reminderId == -1L) return

        Log.i("ReminderReceiver", "Received alarm for reminder $reminderId, preNotify=$isPreNotify")

        val data = ReminderData(context)
        val reminder = data.getAll().find { it.id == reminderId } ?: return

        if (isPreNotify) {
            showPreNotify(context, reminder)
        } else {
            // 启动全屏闹钟Activity
            startFullScreenAlarm(context, reminder)
            // 如果是循环闹钟，重新调度下次
            if (reminder.repeatMode != RepeatMode.ONCE) {
                val scheduler = AlarmScheduler(context)
                scheduler.schedule(reminder)
            }
        }
    }

    private fun startFullScreenAlarm(context: Context, reminder: Reminder) {
        val intent = Intent(context, ReminderAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("reminder_id", reminder.id)
        }
        context.startActivity(intent)
    }

    private fun showPreNotify(context: Context, reminder: Reminder) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("reminder_id", reminder.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (reminder.id * 10 + 1).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ 预备提醒")
            .setContentText("${reminder.title} 将于 ${reminder.preNotifyMinutes} 分钟后触发")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify((reminder.id * 10 + 1).toInt(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}