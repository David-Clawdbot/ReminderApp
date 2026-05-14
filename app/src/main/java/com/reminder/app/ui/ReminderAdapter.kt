package com.reminder.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reminder.app.data.RepeatMode
import com.reminder.app.data.Reminder
import com.reminder.app.databinding.ItemReminderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderAdapter(
    private val onToggle: (Reminder) -> Unit,
    private val onEdit: (Reminder) -> Unit,
    private val onDelete: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemReminderBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        private val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        fun bind(reminder: Reminder) {
            binding.textTime.text = timeFormat.format(Date(reminder.triggerTime))

            // 构建描述文本
            val dateStr = dateFormat.format(Date(reminder.triggerTime))
            val dayStr = dayFormat.format(Date(reminder.triggerTime))
            val repeatStr = when (reminder.repeatMode) {
                RepeatMode.ONCE -> dateStr
                RepeatMode.DAILY -> "每日"
                RepeatMode.WEEKDAYS -> "工作日"
                RepeatMode.WEEKLY -> "每周${dayStr}"
            }
            binding.textRepeat.text = repeatStr

            // 提前提醒
            val preStr = when (reminder.preNotifyMinutes) {
                0 -> ""
                5 -> " · 提前5分钟"
                10 -> "· 提前10分钟"
                else -> ""
            }
            binding.textDescription.text = "${reminder.content}$preStr".trim().ifEmpty { "无备注" }

            // 开关状态
            binding.switchEnabled.isChecked = reminder.isEnabled
            binding.switchEnabled.setOnCheckedChangeListener { _, _ ->
                onToggle(reminder)
            }

            // 按钮
            binding.btnEdit.setOnClickListener { onEdit(reminder) }
            binding.btnDelete.setOnClickListener { onDelete(reminder) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder) = oldItem == newItem
    }
}