package com.reminder.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.reminder.app.R
import com.reminder.app.data.Reminder
import com.reminder.app.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ReminderAdapter

    private val addReminderLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        Log.i(TAG, "=== AddReminder result received ===")
        viewModel.loadReminders()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "=== onCreate START ===")
        try {
            super.onCreate(savedInstanceState)
            Log.i(TAG, "=== super.onCreate done ===")
            binding = ActivityMainBinding.inflate(layoutInflater)
            Log.i(TAG, "=== binding inflated ===")
            setContentView(binding.root)
            Log.i(TAG, "=== setContentView done ===")
            
            viewModel = MainViewModel(application)
            Log.i(TAG, "=== ViewModel created ===")
            
            setupToolbar()
            Log.i(TAG, "=== setupToolbar done ===")
            setupRecyclerView()
            Log.i(TAG, "=== setupRecyclerView done ===")
            setupFab()
            Log.i(TAG, "=== setupFab done ===")
            observeReminders()
            Log.i(TAG, "=== observeReminders done ===")
            Log.i(TAG, "=== onCreate END ===")
        } catch (e: Throwable) {
            Log.e(TAG, "FATAL in onCreate", e)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_all -> {
                    showDeleteAllDialog()
                    true
                }
                else -> false
            }
        }
        binding.toolbar.inflateMenu(R.menu.menu_main)
    }

    private fun setupRecyclerView() {
        adapter = ReminderAdapter(
            onDeleteClick = { reminder -> showDeleteDialog(reminder) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddReminderActivity::class.java)
            addReminderLauncher.launch(intent)
        }
    }

    private fun observeReminders() {
        lifecycleScope.launch {
            viewModel.reminders.collectLatest { reminders ->
                adapter.submitList(reminders)
                binding.emptyView.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (reminders.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun showDeleteDialog(reminder: Reminder) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete)
            .setMessage(getString(R.string.confirm_delete))
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deleteReminder(reminder)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_all)
            .setMessage(R.string.confirm_delete_all)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deleteAllReminders()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
