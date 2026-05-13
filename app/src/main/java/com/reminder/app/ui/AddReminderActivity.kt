package com.reminder.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
    private val TAG = "AddReminder"

    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var data: ReminderData

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var selectedDateStr = ""
    private var selectedTimeStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "=== onCreate START ===")
        try {
            super.onCreate(savedInstanceState)
            Log.i(TAG, "super.onCreate done")

            data = ReminderData(this)
            Log.i(TAG, "ReminderData initialized")

            binding = ActivityAddReminderBinding.inflate(layoutInflater)
            Log.i(TAG, "Binding inflated")

            setContentView(binding.root)
            Log.i(TAG, "setContentView done")

            setupToolbar()
            setupDateButton()
            setupTimeButton()
            setupSaveButton()

            updateDateTimeDisplay()
            Log.i(TAG, "=== onCreate END ===")
        } catch (e: Throwable) {
            Log.e(TAG, "FATAL in onCreate", e)
            // Show crash dialog for debugging
            android.app.AlertDialog.Builder(this)
                .setTitle("AddReminderActivity Error")
                .setMessage("Error: " + e.message + "\n" + android.util.Log.getStackTraceString(e))
                .setPositiveButton("OK") { _, _ -> finish() }
                .show()
        }
    }

    private fun setupToolbar() {
        Log.i(TAG, "Setting up toolbar")
        binding.toolbar.setNavigationOnClickListener {
            Log.i(TAG, "Toolbar back clicked")
            finish()
        }
        binding.toolbar.title = getString(R.string.add_reminder)
    }

    private fun setupDateButton() {
        Log.i(TAG, "Setting up date button")
        binding.btnDate.setOnClickListener {
            Log.i(TAG, "Date button clicked")
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
        Log.i(TAG, "Setting up time button")
        binding.btnTime.setOnClickListener {
            Log.i(TAG, "Time button clicked")
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
        Log.i(TAG, "Setting up save button")
        binding.btnSave.setOnClickListener {
            Log.i(TAG, "Save button clicked")
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
