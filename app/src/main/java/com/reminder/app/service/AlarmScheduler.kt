package com.reminder.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.reminder.app.data.Reminder
import com.reminder.app.data.ReminderData
import com.reminder.app.receiver.ReminderReceiver

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder) {
        if (!reminder.isEnabled) {
            cancel(reminder)
            return
        }

        val nextTime = reminder.getNextTriggerTime() ?: return

        // 预备提醒
        if (reminder.preNotifyMinutes > 0) {
            val preTime = nextTime - reminder.preNotifyMinutes * 60 * 1000L
            if (preTime > System.currentTimeMillis()) {
                scheduleAlarm(reminder, preTime, isPreNotify = true)
            }
        }

        // 主提醒
        scheduleAlarm(reminder, nextTime, isPreNotify = false)
    }

    private fun scheduleAlarm(reminder: Reminder, time: Long, isPreNotify: Boolean) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("is_pre_notify", isPreNotify)
        }

        val requestCode = ((reminder.id * 10) + if (isPreNotify) 1 else 0).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }
            Log.i("AlarmScheduler", "Scheduled alarm for ${reminder.id} at $time (pre=$isPreNotify)")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Failed to schedule alarm", e)
        }
    }

    fun cancel(reminder: Reminder) {
        // 取消主闹钟
        cancelIntent((reminder.id * 10).toInt())
        // 取消预备提醒
        cancelIntent(((reminder.id * 10) + 1).toInt())
    }

    private fun cancelIntent(requestCode: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun rescheduleAll() {
        val data = ReminderData(context)
        data.getAll().forEach { schedule(it) }
    }
}