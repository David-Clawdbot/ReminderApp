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
import android.view.WindowManager
import com.reminder.app.R
import com.reminder.app.data.ReminderData

class ReminderAlarmActivity : Activity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var reminderTitle: String = ""
    private var reminderContent: String = ""
    private var reminderId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        // 全屏显示
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )

        setContentView(R.layout.activity_reminder_alarm)

        reminderId = intent.getLongExtra("reminder_id", -1)
        if (reminderId == -1L) {
            finish()
            return
        }

        // 加载提醒数据
        val data = ReminderData(this)
        val reminder = data.getAll().find { it.id == reminderId }
        if (reminder == null) {
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
        try {
            val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@ReminderAlarmActivity, uri)
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(
                longArrayOf(0, 1000, 500, 1000, 500, 1000),
                intArrayOf(0, 255, 0, 255, 0, 255),
                0
            ))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000), 0)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    private fun cancelNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(reminderId.toInt())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        stopVibration()
    }

    override fun onBackPressed() {
        // 禁止按返回键关闭，必须点按钮
    }
}