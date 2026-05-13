package com.reminder.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String? = null,
    val triggerTime: Long, // 触发时间戳（毫秒）
    val createdAt: Long = System.currentTimeMillis(),
    val isTriggered: Boolean = false
)