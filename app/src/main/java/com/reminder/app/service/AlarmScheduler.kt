package com.reminder.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.reminder.app.data.Reminder
import com.reminder.app.data.ReminderData
import com.reminder.app.receiver.ReminderReceiver

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        // 允许的触发误差（毫秒），避免因系统休眠导致的时间漂移
        private const val WINDOW_TOLERANCE_MS = 1000L
        // 最小提前量，避免在当前时间之前调度
        private const val MIN_ADVANCE_MS = 1000L
    }

    fun schedule(reminder: Reminder) {
        if (!reminder.isEnabled) {
            cancel(reminder)
            return
        }

        val nextTime = reminder.getNextTriggerTime() ?: return
        val now = System.currentTimeMillis()

        Log.i("AlarmScheduler", "Scheduling reminder ${reminder.id}: nextTime=$nextTime, now=$now, diff=${nextTime - now}ms")

        // 确保时间在现在之后（允许1秒的最小提前量避免立即触发）
        if (nextTime <= now + MIN_ADVANCE_MS) {
            Log.w("AlarmScheduler", "Next trigger time is in the past or too close ($nextTime vs $now), rescheduling")
            // 重新计算下一次触发时间
            val nextNextTime = reminder.getNextTriggerTime(System.currentTimeMillis() + 2000)
            if (nextNextTime == null) {
                Log.w("AlarmScheduler", "No valid next trigger time, skipping")
                return
            }
            // 用新的时间继续调度
            scheduleWithTime(reminder, nextNextTime)
            return
        }

        // 预备提醒
        if (reminder.preNotifyMinutes > 0) {
            val preTime = nextTime - reminder.preNotifyMinutes * 60 * 1000L
            if (preTime > now + MIN_ADVANCE_MS) {
                scheduleAlarm(reminder, preTime, isPreNotify = true)
            } else {
                Log.i("AlarmScheduler", "Pre-notify time $preTime is in the past, skipping")
            }
        }

        // 主提醒
        scheduleAlarm(reminder, nextTime, isPreNotify = false)
    }

    private fun scheduleWithTime(reminder: Reminder, nextTime: Long) {
        val now = System.currentTimeMillis()
        Log.i("AlarmScheduler", "Rescheduling with new time: $nextTime, now=$now")

        // 预备提醒
        if (reminder.preNotifyMinutes > 0) {
            val preTime = nextTime - reminder.preNotifyMinutes * 60 * 1000L
            if (preTime > now + MIN_ADVANCE_MS) {
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
            action = "com.reminder.app.ALARM_${reminder.id}_${if (isPreNotify) "PRE" else "MAIN"}"
        }

        val requestCode = getRequestCode(reminder.id, isPreNotify)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 目标时间和当前时间的差值（毫秒）
        val now = System.currentTimeMillis()
        val diff = time - now

        // 如果时间已经非常接近（小于2秒），调整到2秒后避免立即触发
        val safeTime = if (diff < 2000) {
            Log.w("AlarmScheduler", "Target time too close (${diff}ms), adjusting to +2s")
            now + 2000
        } else {
            time
        }

        try {
            // 使用 setAlarmClock 以获得最高优先级（不受Doze限制）
            val alarmClockInfo = AlarmManager.AlarmClockInfo(
                safeTime,
                pendingIntent
            )
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.i("AlarmScheduler", "Using setAlarmClock (highest priority) for ${reminder.id} at $safeTime")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "setAlarmClock failed, falling back to setExactAndAllowWhileIdle", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            safeTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            safeTime,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        safeTime,
                        pendingIntent
                    )
                }
            } catch (e2: Exception) {
                Log.e("AlarmScheduler", "Fallback also failed", e2)
            }
        }
    }

    private fun getRequestCode(reminderId: Long, isPreNotify: Boolean): Int {
        return ((reminderId * 10) + if (isPreNotify) 1 else 0).toInt()
    }

    fun cancel(reminder: Reminder) {
        // 取消主闹钟
        cancelIntent(getRequestCode(reminder.id, false))
        Log.i("AlarmScheduler", "Cancelled main alarm for reminder ${reminder.id}")
        // 取消预备提醒
        cancelIntent(getRequestCode(reminder.id, true))
        Log.i("AlarmScheduler", "Cancelled pre-notify alarm for reminder ${reminder.id}")
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
            try {
                alarmManager.cancel(it)
                it.cancel()
                Log.i("AlarmScheduler", "Cancelled pending intent with requestCode $requestCode")
            } catch (e: Exception) {
                Log.e("AlarmScheduler", "Failed to cancel intent $requestCode", e)
            }
        }
    }

    fun rescheduleAll() {
        Log.i("AlarmScheduler", "Rescheduling all alarms...")
        val data = ReminderData(context)
        val reminders = data.getAll()
        Log.i("AlarmScheduler", "Found ${reminders.size} reminders to reschedule")
        reminders.forEach { reminder ->
            try {
                schedule(reminder)
                Log.i("AlarmScheduler", "Rescheduled reminder ${reminder.id}")
            } catch (e: Exception) {
                Log.e("AlarmScheduler", "Failed to reschedule reminder ${reminder.id}", e)
            }
        }
    }
}