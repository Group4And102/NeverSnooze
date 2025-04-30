package com.example.neversnooze

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CongratulationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congratulations)

        val targetObject = intent.getStringExtra("TARGET_OBJECT")
        val congratsText = findViewById<TextView>(R.id.congratsText)
        congratsText.text = "🎉 Great job! You found the $targetObject!"
    }
}