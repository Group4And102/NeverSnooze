package com.example.neversnooze

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Build
import android.widget.Button
import android.os.Handler
import android.os.Looper
import java.net.URL
import org.json.JSONArray

class CongratulationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congratulations)

        val quoteText = findViewById<TextView>(R.id.quoteText)
        quoteText.text = "Loading daily quote..."

        // Fetch the daily quote
        Thread {
            try {
                val response = URL("https://zenquotes.io/api/random").readText()
                val jsonArray = JSONArray(response)
                val quote = jsonArray.getJSONObject(0).getString("q")
                val author = jsonArray.getJSONObject(0).getString("a")
                val display = "\"$quote\"\n— $author"
                Handler(Looper.getMainLooper()).post {
                    quoteText.text = display
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    quoteText.text = "Could not load quote."
                }
            }
        }.start()

        val congratsText = findViewById<TextView>(R.id.congratsText)
        congratsText.text = "🎉 Great job!!"

        val stopAlarmButton = findViewById<Button>(R.id.stopAlarmButton)
        stopAlarmButton.setOnClickListener {
            stopService(Intent(this, AlarmService::class.java))
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Close all activities and return to normal app flow
        }
        val snoozeButton = findViewById<Button>(R.id.snoozeButton)
        snoozeButton.setOnClickListener {
            stopService(Intent(this, AlarmService::class.java))
            val alarmId      = intent.getLongExtra("ALARM_ID", -1)
            val alarmHour    = intent.getIntExtra("ALARM_HOUR", 0)
            val alarmMinute  = intent.getIntExtra("ALARM_MINUTE", 0)
            val alarmLabel   = intent.getStringExtra("ALARM_LABEL") ?: ""
            val alarmSound   = intent.getStringExtra("ALARM_SOUND") ?: "default_alarm"
            val challenge    = intent.getStringExtra("ALARM_CHALLENGE_TYPE") ?: "Button"
            val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
                action = "com.example.neversnooze.ALARM_TRIGGERED"
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_HOUR", alarmHour)
                putExtra("ALARM_MINUTE", alarmMinute)
                putExtra("ALARM_LABEL", alarmLabel)
                putExtra("ALARM_SOUND", alarmSound)
                putExtra("ALARM_CHALLENGE_TYPE", challenge)
            }

            val requestCode = (alarmId.toInt() * 1000) + 9
            val pending = PendingIntent.getBroadcast(
                this, requestCode, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule alarm exactly 9 minutes from now
            val triggerAt = System.currentTimeMillis() + 9 * 60 * 1000
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
                }
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }

            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}