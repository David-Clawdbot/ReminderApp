package com.reminder.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.reminder.app.R
import com.reminder.app.data.Reminder
import com.reminder.app.data.ReminderData
import com.reminder.app.data.RepeatMode
import com.reminder.app.databinding.ActivityAddReminderBinding
import com.reminder.app.service.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddReminderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var data: ReminderData
    private lateinit var scheduler: AlarmScheduler

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    private var selectedDateStr = ""
    private var selectedTimeStr = ""
    private var editReminderId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        data = ReminderData(this)
        scheduler = AlarmScheduler(this)

        editReminderId = intent.getLongExtra("reminder_id", -1).takeIf { it != -1L }

        setupToolbar()
        setupDateButton()
        setupTimeButton()
        setupRepeatChips()
        setupPresetButton()
        setupSaveButton()

        // 如果是编辑模式，加载现有数据
        editReminderId?.let { loadReminder(it) }

        updateDateTimeDisplay()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = if (editReminderId != null) getString(R.string.edit_reminder) else getString(R.string.add_reminder)
    }

    private fun setupDateButton() {
        binding.btnDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDateStr = dateFormat.format(calendar.time)
                    updateDateTimeDisplay()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimeButton() {
        binding.btnTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    selectedTimeStr = timeFormat.format(calendar.time)
                    updateDateTimeDisplay()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupRepeatChips() {
        binding.chipGroupRepeat.setOnCheckedStateChangeListener { _, checkedIds ->
            val isWeekly = checkedIds.contains(R.id.chipWeekly)
            binding.chipGroupWeekdays.visibility = if (isWeekly) View.VISIBLE else View.GONE
        }
    }

    private fun setupPresetButton() {
        binding.btnPreset.setOnClickListener {
            binding.editContent.setText(getString(R.string.preset_openclaw))
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveReminder()
        }
    }

    private fun loadReminder(id: Long) {
        val reminder = data.getAll().find { it.id == id } ?: return

        binding.editTitle.setText(reminder.title)
        binding.editContent.setText(reminder.content)

        calendar.timeInMillis = reminder.triggerTime
        selectedDateStr = dateFormat.format(calendar.time)
        selectedTimeStr = timeFormat.format(calendar.time)

        // 设置重复模式
        when (reminder.repeatMode) {
            RepeatMode.ONCE -> binding.chipOnce.isChecked = true
            RepeatMode.DAILY -> binding.chipDaily.isChecked = true
            RepeatMode.WEEKDAYS -> binding.chipWeekdays.isChecked = true
            RepeatMode.WEEKLY -> {
                binding.chipWeekly.isChecked = true
                binding.chipGroupWeekdays.visibility = View.VISIBLE
                // 设置周几
                val days = reminder.repeatDays
                binding.chipMon.isChecked = (days and Reminder.MONDAY) != 0
                binding.chipTue.isChecked = (days and Reminder.TUESDAY) != 0
                binding.chipWed.isChecked = (days and Reminder.WEDNESDAY) != 0
                binding.chipThu.isChecked = (days and Reminder.THURSDAY) != 0
                binding.chipFri.isChecked = (days and Reminder.FRIDAY) != 0
                binding.chipSat.isChecked = (days and Reminder.SATURDAY) != 0
                binding.chipSun.isChecked = (days and Reminder.SUNDAY) != 0
            }
        }

        // 设置提前提醒
        when (reminder.preNotifyMinutes) {
            0 -> binding.chipPreOff.isChecked = true
            5 -> binding.chipPre5.isChecked = true
            10 -> binding.chipPre10.isChecked = true
        }

        updateDateTimeDisplay()
    }

    private fun updateDateTimeDisplay() {
        val dateStr = selectedDateStr.ifEmpty { "----/--/--" }
        val timeStr = selectedTimeStr.ifEmpty { "--:--" }
        binding.textSelectedDateTime.text = "$dateStr $timeStr"
    }

    private fun saveReminder() {
        val title = binding.editTitle.text.toString().trim()
        val content = binding.editContent.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.title_required, Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDateStr.isEmpty() || selectedTimeStr.isEmpty()) {
            Toast.makeText(this, R.string.time_required, Toast.LENGTH_SHORT).show()
            return
        }

        if (calendar.timeInMillis <= System.currentTimeMillis() && getSelectedRepeatMode() == RepeatMode.ONCE) {
            Toast.makeText(this, R.string.time_must_be_future, Toast.LENGTH_SHORT).show()
            return
        }

        val repeatMode = getSelectedRepeatMode()
        val repeatDays = if (repeatMode == RepeatMode.WEEKLY) getSelectedWeekdays() else 0
        val preNotify = getSelectedPreNotify()

        val reminder = Reminder(
            id = editReminderId ?: data.getNextId(),
            title = title,
            content = content,
            triggerTime = calendar.timeInMillis,
            repeatMode = repeatMode,
            repeatDays = repeatDays,
            preNotifyMinutes = preNotify,
            isEnabled = true,
            createdAt = if (editReminderId != null) {
                data.getAll().find { it.id == editReminderId }?.createdAt ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                data.save(reminder)
            }
            scheduler.schedule(reminder)
            finish()
        }
    }

    private fun getSelectedRepeatMode(): RepeatMode {
        return when {
            binding.chipDaily.isChecked -> RepeatMode.DAILY
            binding.chipWeekdays.isChecked -> RepeatMode.WEEKDAYS
            binding.chipWeekly.isChecked -> RepeatMode.WEEKLY
            else -> RepeatMode.ONCE
        }
    }

    private fun getSelectedWeekdays(): Int {
        var days = 0
        if (binding.chipMon.isChecked) days += Reminder.MONDAY
        if (binding.chipTue.isChecked) days += Reminder.TUESDAY
        if (binding.chipWed.isChecked) days += Reminder.WEDNESDAY
        if (binding.chipThu.isChecked) days += Reminder.THURSDAY
        if (binding.chipFri.isChecked) days += Reminder.FRIDAY
        if (binding.chipSat.isChecked) days += Reminder.SATURDAY
        if (binding.chipSun.isChecked) days += Reminder.SUNDAY
        return days
    }

    private fun getSelectedPreNotify(): Int {
        return when {
            binding.chipPre5.isChecked -> 5
            binding.chipPre10.isChecked -> 10
            else -> 0
        }
    }
}