package com.example.neversnooze

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
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
            stopService(Intent(this, AlarmService::class.java))  // Stop the alarm
            finish() // Close all activities and return to normal app flow
        }
    }
}