package com.reminder.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.reminder.app.R
import com.reminder.app.data.local.Reminder
import com.reminder.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ReminderAdapter

    private val addReminderLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 自动刷新，Flow会自动更新
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeReminders()
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
            viewModel.allReminders.collectLatest { reminders ->
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
