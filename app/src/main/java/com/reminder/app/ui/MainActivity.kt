package com.reminder.app.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.reminder.app.R
import com.reminder.app.data.Reminder
import com.reminder.app.data.ReminderData
import com.reminder.app.databinding.ActivityMainBinding
import com.reminder.app.service.AlarmScheduler
import com.reminder.app.service.ReminderService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var data: ReminderData
    private lateinit var adapter: ReminderAdapter
    private lateinit var scheduler: AlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        data = ReminderData(this)
        scheduler = AlarmScheduler(this)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeReminders()

        // 启动前台服务
        startReminderService()
    }

    override fun onResume() {
        super.onResume()
        loadReminders()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_all -> {
                    showDeleteAllDialog()
                    true
                }
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
        binding.toolbar.inflateMenu(R.menu.menu_main)
    }

    private fun setupRecyclerView() {
        adapter = ReminderAdapter(
            onToggle = { reminder ->
                val updated = reminder.copy(isEnabled = !reminder.isEnabled)
                data.save(updated)
                scheduler.schedule(updated)
                loadReminders()
            },
            onEdit = { reminder ->
                val intent = Intent(this, AddReminderActivity::class.java).apply {
                    putExtra("reminder_id", reminder.id)
                }
                startActivity(intent)
            },
            onDelete = { reminder ->
                showDeleteDialog(reminder)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddReminderActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeReminders() {
        lifecycleScope.launch {
            data.getAll().let { reminders ->
                adapter.submitList(reminders)
                binding.emptyView.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (reminders.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun loadReminders() {
        val reminders = data.getAll()
        adapter.submitList(reminders)
        binding.emptyView.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (reminders.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showDeleteDialog(reminder: Reminder) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.yes) { _, _ ->
                scheduler.cancel(reminder)
                data.delete(reminder.id)
                loadReminders()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_all)
            .setMessage(R.string.confirm_delete_all)
            .setPositiveButton(R.string.yes) { _, _ ->
                data.getAll().forEach { scheduler.cancel(it) }
                data.deleteAll()
                loadReminders()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun startReminderService() {
        val intent = Intent(this, ReminderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}