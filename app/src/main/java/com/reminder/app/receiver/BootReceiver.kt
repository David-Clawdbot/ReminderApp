package com.reminder.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.reminderapp.data.alarm.ReminderAlarmManager
import com.example.reminderapp.data.local.ReminderDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var alarmManager: ReminderAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 重新设置所有未触发的提醒闹钟
            CoroutineScope(Dispatchers.IO).launch {
                val reminders = reminderDao.getAllActiveRemindersSync()
                val currentTime = System.currentTimeMillis()
                reminders.forEach { reminder ->
                    if (reminder.triggerTime > currentTime) {
                        alarmManager.setExactAlarm(reminder.id, reminder.triggerTime)
                    }
                }
            }
        }
    }
}
