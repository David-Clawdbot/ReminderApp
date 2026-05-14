package com.reminder.app.data

data class Reminder(
    val id: Long,
    val title: String,
    val content: String,
    val triggerTime: Long,         // 首次触发时间戳（毫秒）
    val repeatMode: RepeatMode = RepeatMode.ONCE,
    val repeatDays: Int = 0,       // bitmask: 1=周一, 2=周二, 4=周三, 8=周四, 16=周五, 32=周六, 64=周日
    val preNotifyMinutes: Int = 0,  // 0=不提前, 5, 10
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val MONDAY    = 1
        const val TUESDAY   = 2
        const val WEDNESDAY = 4
        const val THURSDAY  = 8
        const val FRIDAY    = 16
        const val SATURDAY  = 32
        const val SUNDAY    = 64
    }

    fun getNextTriggerTime(fromTime: Long = System.currentTimeMillis()): Long? {
        if (!isEnabled) return null

        return when (repeatMode) {
            RepeatMode.ONCE -> {
                if (triggerTime > fromTime) triggerTime else null
            }
            RepeatMode.DAILY -> {
                var next = triggerTime
                while (next <= fromTime) {
                    next += 24 * 60 * 60 * 1000L
                }
                next
            }
            RepeatMode.WEEKDAYS -> {
                var next = triggerTime
                while (next <= fromTime) {
                    next += 24 * 60 * 60 * 1000L
                    val dayOfWeek = java.util.Calendar.getInstance().apply { timeInMillis = next }.get(java.util.Calendar.DAY_OF_WEEK)
                    // 周一到周五 = Calendar.MONDAY(2) 到 Calendar.FRIDAY(6)
                    if (dayOfWeek < java.util.Calendar.MONDAY || dayOfWeek > java.util.Calendar.FRIDAY) {
                        // 跳过周末，但这个逻辑需要修正
                    }
                }
                next
            }
            RepeatMode.WEEKLY -> {
                if (repeatDays == 0) return null
                var next = triggerTime
                while (next <= fromTime) {
                    next += 7 * 24 * 60 * 60 * 1000L
                }
                // 找到下一个匹配的星期几
                next
            }
        }
    }
}