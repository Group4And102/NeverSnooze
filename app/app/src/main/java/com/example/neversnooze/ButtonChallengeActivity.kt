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
                val intent = Intent(this, CongratulationsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        // Prevent going back
        moveTaskToBack(true)
    }
} 