package com.reminder.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.app.data.alarm.ReminderAlarmManager
import com.reminder.app.data.local.Reminder
import com.reminder.app.data.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmManager: ReminderAlarmManager
) : ViewModel() {

    fun addReminder(title: String, content: String?, triggerTime: Long) {
        viewModelScope.launch {
            val reminder = Reminder(
                title = title,
                content = content,
                triggerTime = triggerTime
            )
            val id = repository.insertReminder(reminder)
            // 设置闹钟
            alarmManager.setExactAlarm(id, triggerTime)
        }
    }
}
