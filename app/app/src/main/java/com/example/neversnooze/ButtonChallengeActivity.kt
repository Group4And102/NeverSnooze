package com.example.neversnooze

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.provider.Settings

class ButtonChallengeActivity : AppCompatActivity() {
    private lateinit var counterText: TextView
    private lateinit var pressButton: MaterialButton
    private var pressCount = 0
    private var alarmId: Long = -1
    private var alarmHour: Int = 0
    private var alarmMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button_challenge)

        // Initialize views
        counterText = findViewById(R.id.counterText)
        pressButton = findViewById(R.id.pressButton)

        // Get alarm details from intent
        alarmId = intent.getLongExtra("ALARM_ID", -1)
        alarmHour = intent.getIntExtra("ALARM_HOUR", 0)
        alarmMinute = intent.getIntExtra("ALARM_MINUTE", 0)

        // Set up button click listener
        pressButton.setOnClickListener {
            pressCount++
            counterText.text = "$pressCount/10"
            
            if (pressCount >= 10) {
                // Challenge completed, snooze the alarm
                val stopIntent = Intent(this, AlarmService::class.java)
                stopService(stopIntent)
                if (canScheduleExactAlarms()) {
                    snoozeAlarm()
                } else {
                    requestExactAlarmPermission()
                }
                finish()
            }
        }
    }

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }
    }

    private fun snoozeAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = "com.example.neversnooze.ALARM_TRIGGERED"
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", alarmHour)
            putExtra("ALARM_MINUTE", alarmMinute)
            putExtra("ALARM_LABEL", intent.getStringExtra("ALARM_LABEL") ?: "")
            putExtra("ALARM_SOUND", intent.getStringExtra("ALARM_SOUND") ?: "default_alarm")
        }

        // Schedule the alarm for 5 minutes later
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(java.util.Calendar.MINUTE, 5)
        }

        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            alarmId.toInt(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    override fun onBackPressed() {
        // Prevent going back
        moveTaskToBack(true)
    }
} 