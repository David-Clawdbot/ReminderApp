package com.reminder.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class Reminder(
    val id: Long,
    val title: String,
    val content: String,
    val triggerTime: Long,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

class ReminderData(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)

    fun getAll(): List<Reminder> {
        val json = prefs.getString("reminders", "[]") ?: "[]"
        val list = mutableListOf<Reminder>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(Reminder(
                id = obj.getLong("id"),
                title = obj.getString("title"),
                content = obj.optString("content", ""),
                triggerTime = obj.getLong("triggerTime"),
                isEnabled = obj.optBoolean("isEnabled", true),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            ))
        }
        return list.sortedBy { it.triggerTime }
    }

    fun save(reminder: Reminder) {
        val list = getAll().toMutableList()
        list.removeAll { it.id == reminder.id }
        list.add(reminder)
        saveAll(list)
        // Schedule the alarm
        if (reminder.isEnabled && reminder.triggerTime > System.currentTimeMillis()) {
            ReminderScheduler.schedule(context, reminder)
        }
    }

    fun delete(id: Long) {
        ReminderScheduler.cancel(context, id)
        val list = getAll().toMutableList()
        list.removeAll { it.id == id }
        saveAll(list)
    }

    fun deleteAll() {
        getAll().forEach { ReminderScheduler.cancel(context, it.id) }
        prefs.edit().putString("reminders", "[]").apply()
    }

    fun rescheduleAll() {
        getAll().forEach { reminder ->
            if (reminder.isEnabled && reminder.triggerTime > System.currentTimeMillis()) {
                ReminderScheduler.schedule(context, reminder)
            }
        }
    }

    private fun saveAll(list: List<Reminder>) {
        val array = JSONArray()
        for (r in list) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("content", r.content)
                put("triggerTime", r.triggerTime)
                put("isEnabled", r.isEnabled)
                put("createdAt", r.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString("reminders", array.toString()).apply()
    }

    fun getNextId(): Long = (getAll().maxOfOrNull { it.id } ?: 0) + 1
}
