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
<<<<<<< Updated upstream
            stopService(Intent(this, AlarmService::class.java))  // Stop the alarm
            finish() // Close all activities and return to normal app flow
=======
            stopService(Intent(this, AlarmService::class.java))
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun getWeatherForCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    fetchWeatherIcon(it.latitude, it.longitude)
                } ?: run {
                    // Fallback to default location if lastLocation is null
                    fetchWeatherIcon(40.7128, -74.0060) // Default to New York
                }
            }.addOnFailureListener { e ->
                // Fallback if location fails
                fetchWeatherIcon(40.7128, -74.0060) // Default to New York
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun fetchWeatherIcon(lat: Double, lon: Double) {
        Thread {
            try {
                val url = URL("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric")
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    requestMethod = "GET"
                }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val weatherArray = json.getJSONArray("weather")
                val iconCode = weatherArray.getJSONObject(0).getString("icon")
                val iconUrl = "https://openweathermap.org/img/wn/$iconCode@4x.png"

                runOnUiThread {
                    Glide.with(this@CongratulationsActivity)
                        .load(iconUrl)
                        .placeholder(R.drawable.ic_weather_loading)
                        .error(R.drawable.ic_weather_default)
                        .into(weatherIcon)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    weatherIcon.setImageResource(R.drawable.ic_weather_default)
                }
            }
        }.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getWeatherForCurrentLocation()
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
>>>>>>> Stashed changes
        }
    }
}