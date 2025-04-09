package com.example.neversnooze

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class CreateAlarm : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.alarm_create)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val purpleColor = ContextCompat.getColor(this, R.color.purple_500)
        val grayColor = Color.parseColor("#444444")
        val button = findViewById<MaterialButton>(R.id.createAlarmButton)

        val dayButtons = listOf(
            findViewById<MaterialButton>(R.id.btnSun),
            findViewById(R.id.btnMon),
            findViewById(R.id.btnTue),
            findViewById(R.id.btnWed),
            findViewById(R.id.btnThu),
            findViewById(R.id.btnFri),
            findViewById(R.id.btnSat)
        )

        dayButtons.forEach { button ->
            button.setOnClickListener {
                // Toggle manually
                val nowChecked = !button.isSelected
                button.isSelected = nowChecked
                button.backgroundTintList = ColorStateList.valueOf(
                    if (nowChecked) purpleColor else grayColor
                )
            }
        }

        button.setOnClickListener {
            // Nothing yet, just to trigger ripple
        }
    }
}