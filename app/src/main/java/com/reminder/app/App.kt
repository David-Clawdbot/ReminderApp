package com.reminder.app

import android.app.Application
import android.util.Log

class App : Application() {
    companion object {
        private const val TAG = "ReminderApp"
    }

    override fun onCreate() {
        Log.i(TAG, "=== App onCreate ===")
        super.onCreate()
    }
}
