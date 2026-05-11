package com.reminder.app

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 提醒数据模型
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,           // 提醒标题
    val content: String,         // 提醒详情
    val triggerTime: Long,       // 触发时间(毫秒时间戳)
    val isEnabled: Boolean = true,  // 是否启用
    val createdAt: Long = System.currentTimeMillis()  // 创建时间
)