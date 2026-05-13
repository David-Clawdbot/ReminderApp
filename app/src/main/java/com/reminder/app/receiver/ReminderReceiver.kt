package com.reminder.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.reminder.app.data.alarm.ReminderAlarmManager
import com.reminder.app.data.local.ReminderDao
import com.reminder.app.data.notification.ReminderNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var notificationManager: ReminderNotificationManager

    @Inject
    lateinit var alarmManager: ReminderAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.reminder.app.TRIGGER_REMINDER") {
            val reminderId = intent.getLongExtra(ReminderAlarmManager.EXTRA_REMINDER_ID, -1)
            if (reminderId != -1L) {
                CoroutineScope(Dispatchers.IO).launch {
                    val reminder = reminderDao.getReminderById(reminderId)
                    if (reminder != null && !reminder.isTriggered) {
                        // 显示通知
                        notificationManager.showReminderNotification(
                            reminderId,
                            reminder.title,
                            reminder.content
                        )
                        // 标记为已触发
                        reminderDao.markAsTriggered(reminderId)
                        // 取消闹钟
                        alarmManager.cancelAlarm(reminderId)
                    }
                }
            }
        }
    }
}
