package com.example.neversnooze

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button

class CongratulationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congratulations)

        val targetObject = intent.getStringExtra("TARGET_OBJECT")
        val congratsText = findViewById<TextView>(R.id.congratsText)
        congratsText.text = "🎉 Great job! You found the $targetObject!"

        val stopAlarmButton = findViewById<Button>(R.id.stopAlarmButton)
        stopAlarmButton.setOnClickListener {
            stopService(Intent(this, AlarmService::class.java))  // Stop the alarm
            finish() // Close all activities and return to normal app flow
        }
    }
}