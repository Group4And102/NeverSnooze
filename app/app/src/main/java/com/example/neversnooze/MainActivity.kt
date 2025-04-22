package com.example.neversnooze

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neversnooze.alarmscheduler.AndroidAlarmScheduler
import com.example.neversnooze.alarmscheduler.AlarmItem
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /* sets alarm for 5 second after the app is opened for testing */
        val alarmScheduler = AndroidAlarmScheduler(this)
        val alarmTime = LocalDateTime.now().plusSeconds(5)

        val testAlarm = AlarmItem(
            time = alarmTime,
            label = "WAKE UP!"
        )
        alarmScheduler.schedule(testAlarm)
    }
}