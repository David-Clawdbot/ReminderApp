package com.reminder.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.reminder.app.R
import com.reminder.app.data.Reminder
import com.reminder.app.data.ReminderData
import com.reminder.app.databinding.ActivityAddReminderBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddReminderActivity : AppCompatActivity() {
    private val TAG = "AddReminderActivity"

    private lateinit var binding: ActivityAddReminderBinding
    private val data by lazy { ReminderData(this) }

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var selectedDateStr = ""
    private var selectedTimeStr = ""

    private val dateTimeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "=== onCreate START ===")
        try {
            super.onCreate(savedInstanceState)
            binding = ActivityAddReminderBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupToolbar()
            setupDateButton()
            setupTimeButton()
            setupSaveButton()

            updateDateTimeDisplay()
            Log.i(TAG, "=== onCreate END ===")
        } catch (e: Throwable) {
            Log.e(TAG, "FATAL in onCreate", e)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
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

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val content = binding.editContent.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, R.string.reminder_title, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDateStr.isEmpty() || selectedTimeStr.isEmpty()) {
                Toast.makeText(this, R.string.select_datetime, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reminder = Reminder(
                id = data.getNextId(),
                title = title,
                content = content,
                triggerTime = calendar.timeInMillis
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    data.save(reminder)
                }
                Log.i(TAG, "Saved reminder: $title")
                finish()
            }
        }
    }

    private fun updateDateTimeDisplay() {
        val dateStr = selectedDateStr.ifEmpty { "选择日期" }
        val timeStr = selectedTimeStr.ifEmpty { "选择时间" }
        binding.textSelectedDateTime.text = "$dateStr $timeStr"
    }
}
