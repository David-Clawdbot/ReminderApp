package com.reminder.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.reminder.app.service.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.i("BootReceiver", "Boot completed, rescheduling alarms")
                AlarmScheduler(context).rescheduleAll()
            }
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                Log.i("BootReceiver", "Time changed, rescheduling alarms")
                AlarmScheduler(context).rescheduleAll()
            }
            Intent.ACTION_LOCALE_CHANGED -> {
                Log.i("BootReceiver", "Locale changed, rescheduling alarms")
                AlarmScheduler(context).rescheduleAll()
            }
        }
    }
}