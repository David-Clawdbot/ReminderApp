package com.reminder.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.reminder.app.service.AlarmScheduler

class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED -> {
                Log.i("TimeChangeReceiver", "Time/timezone/locale changed, rescheduling alarms")
                try {
                    AlarmScheduler(context).rescheduleAll()
                    Log.i("TimeChangeReceiver", "Successfully rescheduled all alarms")
                } catch (e: Exception) {
                    Log.e("TimeChangeReceiver", "Failed to reschedule alarms", e)
                }
            }
        }
    }
}