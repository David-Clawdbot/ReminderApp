package com.reminder.app.ui

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.WindowManager
import com.reminder.app.R
import com.reminder.app.data.ReminderData

class ReminderAlarmActivity : Activity() {
    private val PREFS_NAME = "settings"
    private val KEY_RINGTONE = "ringtone"

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var reminderTitle: String = ""
    private var reminderContent: String = ""
    private var reminderId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // All the flags needed to show on lock screen and turn screen on
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_ALWAYS_FOCUSABLE
        )

        // For Android 8+ (Oreo), use the proper API methods
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        setContentView(R.layout.activity_reminder_alarm)

        reminderId = intent.getLongExtra("reminder_id", -1)
        if (reminderId == -1L) {
            Log.e("ReminderAlarm", "No reminder_id")
            finish()
            return
        }

        val data = ReminderData(this)
        val reminder = data.getAll().find { it.id == reminderId }
        if (reminder == null) {
            Log.e("ReminderAlarm", "Reminder $reminderId not found")
            finish()
            return
        }

        reminderTitle = reminder.title
        reminderContent = reminder.content.ifEmpty { "时间到！" }

        setupViews()
        startRingtone()
        startVibration()
        cancelNotification()
    }

    private fun setupViews() {
        findViewById<android.widget.TextView>(R.id.textAlarmTitle).text = reminderTitle
        findViewById<android.widget.TextView>(R.id.textAlarmContent).text = reminderContent
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDismiss).setOnClickListener {
            stopRingtone()
            stopVibration()
            finish()
        }
    }

    private fun startRingtone() {
        val ringtoneUri = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_RINGTONE, null)

        val uri: Uri = if (!ringtoneUri.isNullOrEmpty()) {
            Log.i("ReminderAlarm", "Using user ringtone: $ringtoneUri")
            Uri.parse(ringtoneUri)
        } else {
            Log.i("ReminderAlarm", "Using default alarm")
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@ReminderAlarmActivity, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            Log.i("ReminderAlarm", "Ringtone started")
        } catch (e: Exception) {
            Log.e("ReminderAlarm", "Ringtone error", e)
            // Fallback to default
            try {
                val fallback = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@ReminderAlarmActivity, fallback)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e2: Exception) {
                Log.e("ReminderAlarm", "Fallback ringtone error", e2)
            }
        }
    }

    private fun stopRingtone() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("ReminderAlarm", "stop ringtone error", e)
        }
        mediaPlayer = null
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 1000, 500, 1000, 500, 1000),
                        intArrayOf(0, 255, 0, 255, 0, 255),
                        0
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000), 0)
            }
        } catch (e: Exception) {
            Log.e("ReminderAlarm", "vibration error", e)
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("ReminderAlarm", "stop vibration error", e)
        }
    }

    private fun cancelNotification() {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(reminderId.toInt())
            nm.cancel((reminderId * 10 + 1).toInt())
        } catch (e: Exception) {
            Log.e("ReminderAlarm", "cancel notification error", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        stopVibration()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // 禁止返回键关闭，必须点按钮
    }
}