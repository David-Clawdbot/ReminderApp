package com.example.reminderapp.data.repository

import com.example.reminderapp.data.local.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllActiveReminders(): Flow<List<Reminder>>
    fun getAllReminders(): Flow<List<Reminder>>
    suspend fun getReminderById(id: Long): Reminder?
    suspend fun getAllActiveRemindersSync(): List<Reminder>
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
    suspend fun deleteReminderById(id: Long)
    suspend fun deleteAllReminders()
    suspend fun markAsTriggered(id: Long)
}