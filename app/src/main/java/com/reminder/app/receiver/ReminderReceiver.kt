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
import androidx.core.app.NotificationCompat
import com.reminder.app.R
import com.reminder.app.data.Reminder
import com.reminder.app.data.ReminderData
import com.reminder.app.service.AlarmScheduler
import com.reminder.app.ui.MainActivity

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1)
        val isPreNotify = intent.getBooleanExtra("is_pre_notify", false)

        if (reminderId == -1L) return

        val data = ReminderData(context)
        val reminder = data.getAll().find { it.id == reminderId } ?: return

        if (isPreNotify) {
            showPreNotify(context, reminder)
        } else {
            showReminder(context, reminder)
            // 如果是循环闹钟，重新调度下次
            if (reminder.repeatMode != com.reminder.app.data.RepeatMode.ONCE) {
                val scheduler = AlarmScheduler(context)
                scheduler.schedule(reminder)
            }
        }
    }

    private fun showPreNotify(context: Context, reminder: Reminder) {
        val notification = createNotification(
            context,
            "预备提醒",
            "${reminder.title} 将于 ${reminder.preNotifyMinutes} 分钟后触发",
            reminder.id,
            isPreNotify = true
        )
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify((reminder.id * 10 + 1).toInt(), notification)
    }

    private fun showReminder(context: Context, reminder: Reminder) {
        val notification = createNotification(
            context,
            reminder.title,
            reminder.content.ifEmpty { "时间到！" },
            reminder.id,
            isPreNotify = false
        )
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify((reminder.id * 10).toInt(), notification)

        // 响铃
        try {
            val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone.play()
            // 10秒后停止
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                ringtone.stop()
            }, 10000)
        } catch (e: Exception) {
            // ignore
        }

        // 震动
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun createNotification(
        context: Context,
        title: String,
        content: String,
        reminderId: Long,
        isPreNotify: Boolean
    ): android.app.Notification {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = if (isPreNotify) {
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        } else {
            android.app.NotificationManager.IMPORTANCE_HIGH
        }

        return NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                context.getString(R.string.notification_channel_name),
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                enableVibration(true)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}