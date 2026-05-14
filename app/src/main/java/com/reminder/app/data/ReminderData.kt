package com.reminder.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class ReminderData(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)

    fun getAll(): List<Reminder> {
        val json = prefs.getString("reminders", "[]") ?: "[]"
        val list = mutableListOf<Reminder>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(fromJson(obj))
        }
        return list.sortedBy { it.triggerTime }
    }

    fun save(reminder: Reminder) {
        val list = getAll().toMutableList()
        list.removeAll { it.id == reminder.id }
        list.add(reminder)
        saveAll(list)
    }

    fun delete(id: Long) {
        val list = getAll().toMutableList()
        list.removeAll { it.id == id }
        saveAll(list)
    }

    fun deleteAll() {
        prefs.edit().putString("reminders", "[]").apply()
    }

    private fun saveAll(list: List<Reminder>) {
        val array = JSONArray()
        for (r in list) {
            array.put(toJson(r))
        }
        prefs.edit().putString("reminders", array.toString()).apply()
    }

    fun getNextId(): Long = (getAll().maxOfOrNull { it.id } ?: 0) + 1

    private fun toJson(r: Reminder): JSONObject = JSONObject().apply {
        put("id", r.id)
        put("title", r.title)
        put("content", r.content)
        put("triggerTime", r.triggerTime)
        put("repeatMode", r.repeatMode.name)
        put("repeatDays", r.repeatDays)
        put("preNotifyMinutes", r.preNotifyMinutes)
        put("isEnabled", r.isEnabled)
        put("createdAt", r.createdAt)
    }

    private fun fromJson(obj: JSONObject): Reminder {
        val repeatModeStr = obj.optString("repeatMode", "ONCE")
        val repeatMode = try {
            RepeatMode.valueOf(repeatModeStr)
        } catch (e: Exception) {
            RepeatMode.ONCE
        }
        return Reminder(
            id = obj.getLong("id"),
            title = obj.getString("title"),
            content = obj.optString("content", ""),
            triggerTime = obj.getLong("triggerTime"),
            repeatMode = repeatMode,
            repeatDays = obj.optInt("repeatDays", 0),
            preNotifyMinutes = obj.optInt("preNotifyMinutes", 0),
            isEnabled = obj.optBoolean("isEnabled", true),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        )
    }
}