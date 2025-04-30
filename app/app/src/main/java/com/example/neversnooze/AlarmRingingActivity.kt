package com.example.neversnooze

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class AlarmRingingActivity : AppCompatActivity() {
    private lateinit var snoozeButton: MaterialButton
    private var alarmId: Long = -1
    private var alarmHour: Int = 0
    private var alarmMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_ringing)

        // Initialize views
        snoozeButton = findViewById(R.id.snoozeButton)

        // Get alarm details from intent
        alarmId = intent.getLongExtra("ALARM_ID", -1)
        alarmHour = intent.getIntExtra("ALARM_HOUR", 0)
        alarmMinute = intent.getIntExtra("ALARM_MINUTE", 0)

        // Set up snooze button
        snoozeButton.setOnClickListener {
            startButtonChallenge()
        }
    }

    private fun startButtonChallenge() {
        val intent = Intent(this, ButtonChallengeActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", alarmHour)
            putExtra("ALARM_MINUTE", alarmMinute)
            putExtra("ALARM_LABEL", this@AlarmRingingActivity.intent.getStringExtra("ALARM_LABEL") ?: "")
            putExtra("ALARM_SOUND", this@AlarmRingingActivity.intent.getStringExtra("ALARM_SOUND") ?: "default_alarm")
        }
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent going back to previous screen
        moveTaskToBack(true)
    }
} 