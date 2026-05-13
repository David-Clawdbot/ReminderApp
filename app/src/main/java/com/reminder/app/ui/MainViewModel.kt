package com.reminder.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.app.data.Reminder
import com.reminder.app.data.ReminderData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val data = ReminderData(application)

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            _reminders.value = data.getAll()
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            data.delete(reminder.id)
            loadReminders()
        }
    }

    fun deleteAllReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            data.deleteAll()
            loadReminders()
        }
    }

    fun addReminder(title: String, content: String, triggerTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val reminder = Reminder(
                id = data.getNextId(),
                title = title,
                content = content,
                triggerTime = triggerTime
            )
            data.save(reminder)
            loadReminders()
        }
    }
}
