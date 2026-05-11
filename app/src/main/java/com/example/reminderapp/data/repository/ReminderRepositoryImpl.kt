package com.example.reminderapp.data.repository

import com.example.reminderapp.data.local.Reminder
import com.example.reminderapp.data.local.ReminderDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAllActiveReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllActiveReminders()
    }

    override fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders()
    }

    override suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getReminderById(id)
    }

    override suspend fun getAllActiveRemindersSync(): List<Reminder> {
        return reminderDao.getAllActiveRemindersSync()
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    override suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)
    }

    override suspend fun deleteAllReminders() {
        reminderDao.deleteAllReminders()
    }

    override suspend fun markAsTriggered(id: Long) {
        reminderDao.markAsTriggered(id)
    }
}