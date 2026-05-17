package com.reminder.app.data

import java.util.Calendar

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
        const val MONDAY    = 1    // 0b0000001
        const val TUESDAY   = 2    // 0b0000010
        const val WEDNESDAY = 4    // 0b0000100
        const val THURSDAY  = 8    // 0b0001000
        const val FRIDAY    = 16   // 0b0010000
        const val SATURDAY  = 32   // 0b0100000
        const val SUNDAY    = 64   // 0b1000000
    }

    /**
     * 计算下一次触发时间
     * @param fromTime 参考时间（默认当前时间），用于计算相对时间
     * @return 下一次触发的时间戳，如果已过期且是单次提醒返回 null
     */
    fun getNextTriggerTime(fromTime: Long = System.currentTimeMillis()): Long? {
        if (!isEnabled) return null

        return when (repeatMode) {
            RepeatMode.ONCE -> {
                // 单次：只在未来时间有效
                if (triggerTime > fromTime) triggerTime else null
            }
            RepeatMode.DAILY -> {
                // 每日：计算同时间的下一个日期
                calculateDailyNext(fromTime)
            }
            RepeatMode.WEEKDAYS -> {
                // 工作日：周一到周五
                calculateWeekdaysNext(fromTime)
            }
            RepeatMode.WEEKLY -> {
                // 每周：按 repeatDays bitmask 选择星期几
                calculateWeeklyNext(fromTime)
            }
        }
    }

    private fun calculateDailyNext(fromTime: Long): Long {
        var next = triggerTime
        val oneDayMs = 24 * 60 * 60 * 1000L

        // 如果已过期，计算下一个匹配日期
        while (next <= fromTime) {
            next += oneDayMs
        }

        // 提取触发时间的小时和分钟
        val triggerCal = Calendar.getInstance().apply { timeInMillis = triggerTime }
        val triggerHour = triggerCal.get(Calendar.HOUR_OF_DAY)
        val triggerMinute = triggerCal.get(Calendar.MINUTE)
        val triggerSecond = triggerCal.get(Calendar.SECOND)

        // 调整到目标日期的相同时间
        val resultCal = Calendar.getInstance().apply {
            timeInMillis = next
            set(Calendar.HOUR_OF_DAY, triggerHour)
            set(Calendar.MINUTE, triggerMinute)
            set(Calendar.SECOND, triggerSecond)
            set(Calendar.MILLISECOND, 0)
        }

        return resultCal.timeInMillis
    }

    private fun calculateWeekdaysNext(fromTime: Long): Long {
        val oneDayMs = 24 * 60 * 60 * 1000L
        var next = triggerTime

        // 如果已过期，计算到下一个工作日
        while (next <= fromTime) {
            next += oneDayMs
        }

        // 提取触发时间的小时和分钟
        val triggerCal = Calendar.getInstance().apply { timeInMillis = triggerTime }
        val triggerHour = triggerCal.get(Calendar.HOUR_OF_DAY)
        val triggerMinute = triggerCal.get(Calendar.MINUTE)
        val triggerSecond = triggerCal.get(Calendar.SECOND)

        // 找到下一个工作日（周一到周五）
        val resultCal = Calendar.getInstance().apply {
            timeInMillis = next
            set(Calendar.HOUR_OF_DAY, triggerHour)
            set(Calendar.MINUTE, triggerMinute)
            set(Calendar.SECOND, triggerSecond)
            set(Calendar.MILLISECOND, 0)
        }

        // 检查并跳过周末
        var maxIterations = 7 // 最多循环7次找到工作日
        while (maxIterations > 0) {
            val dayOfWeek = resultCal.get(Calendar.DAY_OF_WEEK)
            // Calendar.MONDAY = 2, FRIDAY = 6
            if (dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY) {
                break // 找到工作日
            }
            // 不是工作日，往后加一天
            resultCal.add(Calendar.DAY_OF_MONTH, 1)
            maxIterations--
        }

        // 如果结果仍在 fromTime 之前，往后加一周重新计算
        if (resultCal.timeInMillis <= fromTime) {
            resultCal.timeInMillis = fromTime
            resultCal.add(Calendar.DAY_OF_MONTH, 1)
            return calculateWeekdaysNext(resultCal.timeInMillis)
        }

        return resultCal.timeInMillis
    }

    private fun calculateWeeklyNext(fromTime: Long): Long {
        if (repeatDays == 0) return triggerTime // 没有设置周几，默认用触发时间

        val oneDayMs = 24 * 60 * 60 * 1000L

        // 提取触发时间的小时和分钟
        val triggerCal = Calendar.getInstance().apply { timeInMillis = triggerTime }
        val triggerHour = triggerCal.get(Calendar.HOUR_OF_DAY)
        val triggerMinute = triggerCal.get(Calendar.MINUTE)
        val triggerSecond = triggerCal.get(Calendar.SECOND)

        var nextCal = Calendar.getInstance().apply {
            timeInMillis = if (triggerTime > fromTime) triggerTime else fromTime
            set(Calendar.HOUR_OF_DAY, triggerHour)
            set(Calendar.MINUTE, triggerMinute)
            set(Calendar.SECOND, triggerSecond)
            set(Calendar.MILLISECOND, 0)
        }

        // 如果计算出的时间在 fromTime 之前，调整到 fromTime
        if (nextCal.timeInMillis <= fromTime) {
            nextCal.timeInMillis = fromTime
            nextCal.set(Calendar.HOUR_OF_DAY, triggerHour)
            nextCal.set(Calendar.MINUTE, triggerMinute)
            nextCal.set(Calendar.SECOND, triggerSecond)
            nextCal.set(Calendar.MILLISECOND, 0)
        }

        // 最多循环14次找到匹配的星期
        var iterations = 14
        while (iterations > 0) {
            val dayOfWeek = nextCal.get(Calendar.DAY_OF_WEEK)
            val targetDayBit = when (dayOfWeek) {
                Calendar.MONDAY -> MONDAY
                Calendar.TUESDAY -> TUESDAY
                Calendar.WEDNESDAY -> WEDNESDAY
                Calendar.THURSDAY -> THURSDAY
                Calendar.FRIDAY -> FRIDAY
                Calendar.SATURDAY -> SATURDAY
                Calendar.SUNDAY -> SUNDAY
                else -> 0
            }

            // 检查这一天的bitmask是否设置
            if ((repeatDays and targetDayBit) != 0) {
                // 找到了匹配的星期几
                if (nextCal.timeInMillis > fromTime) {
                    return nextCal.timeInMillis
                }
            }

            // 没找到，往后加一天
            nextCal.add(Calendar.DAY_OF_MONTH, 1)
            iterations--
        }

        // 如果14天内没找到（比如bitmask设置的星期都在过去），往后加7天重试
        nextCal.add(Calendar.DAY_OF_MONTH, 7)
        return calculateWeeklyNext(nextCal.timeInMillis)
    }
}