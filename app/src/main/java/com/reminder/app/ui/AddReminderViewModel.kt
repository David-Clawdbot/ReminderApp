package com.reminder.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.reminder.app.data.Reminder
import com.reminder.app.data.ReminderData

class AddReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val data = ReminderData(application)

    fun saveReminder(title: String, content: String, triggerTime: Long) {
        val reminder = Reminder(
            id = data.getNextId(),
            title = title,
            content = content,
            triggerTime = triggerTime
        )
        data.save(reminder)
    }
}
