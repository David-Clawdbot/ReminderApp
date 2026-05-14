package com.reminder.app.ui

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.reminder.app.R
import com.reminder.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private val PREFS_NAME = "settings"
    private val KEY_KEEP_ALIVE = "keep_alive"
    private val KEY_DARK_MODE = "dark_mode"
    private val KEY_RINGTONE = "ringtone"
    private val KEY_DEFAULT_PRE_NOTIFY = "default_pre_notify"

    private var currentRingtoneUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSettings()
        setupToolbar()
        setupRingtone()
        setupDefaultPreNotify()
        setupKeepAlive()
        setupDarkMode()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        currentRingtoneUri = prefs.getString(KEY_RINGTONE, null)
        binding.switchKeepAlive.isChecked = prefs.getBoolean(KEY_KEEP_ALIVE, true)
        binding.switchDarkMode.isChecked = prefs.getBoolean(KEY_DARK_MODE, false)

        val preNotify = prefs.getInt(KEY_DEFAULT_PRE_NOTIFY, 0)
        binding.textDefaultPreNotify.text = when (preNotify) {
            5 -> getString(R.string.pre_notify_5)
            10 -> getString(R.string.pre_notify_10)
            else -> getString(R.string.pre_notify_off)
        }
    }

    private fun saveSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
        prefs.putString(KEY_RINGTONE, currentRingtoneUri)
        prefs.putBoolean(KEY_KEEP_ALIVE, binding.switchKeepAlive.isChecked)
        prefs.putBoolean(KEY_DARK_MODE, binding.switchDarkMode.isChecked)
        prefs.apply()
    }

    private fun setupRingtone() {
        updateRingtoneDisplay()

        binding.layoutRingtone.setOnClickListener {
            try {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtone))
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    currentRingtoneUri?.let { putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(it)) }
                }
                @Suppress("DEPRECATION")
                startActivityForResult(intent, 100)
            } catch (e: Exception) {
                Toast.makeText(this, "无法选择铃声", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRingtoneDisplay() {
        val uri = currentRingtoneUri?.let { Uri.parse(it) }
        if (uri != null) {
            try {
                val ringtone = RingtoneManager.getRingtone(this, uri)
                binding.textRingtone.text = ringtone?.getTitle(this) ?: getString(R.string.ringtone_title)
            } catch (e: Exception) {
                binding.textRingtone.text = getString(R.string.ringtone_title)
            }
        } else {
            binding.textRingtone.text = getString(R.string.ringtone_title) + " (默认)"
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)?.let { uri ->
                currentRingtoneUri = uri.toString()
                updateRingtoneDisplay()
                saveSettings()
            }
        }
    }

    private fun setupDefaultPreNotify() {
        binding.layoutDefaultPreNotify.setOnClickListener {
            val options = arrayOf(
                getString(R.string.pre_notify_off),
                getString(R.string.pre_notify_5),
                getString(R.string.pre_notify_10)
            )
            AlertDialog.Builder(this)
                .setTitle(R.string.default_pre_notify_title)
                .setItems(options) { _, which ->
                    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    prefs.putInt(KEY_DEFAULT_PRE_NOTIFY, when (which) {
                        1 -> 5
                        2 -> 10
                        else -> 0
                    })
                    prefs.apply()
                    binding.textDefaultPreNotify.text = options[which]
                }
                .show()
        }
    }

    private fun setupKeepAlive() {
        binding.switchKeepAlive.setOnCheckedChangeListener { _, isChecked ->
            saveSettings()
        }
    }

    private fun setupDarkMode() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            saveSettings()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}