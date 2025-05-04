package com.example.neversnooze

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CongratulationsActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST = 1001
    private val apiKey = WeatherConfig.API_KEY// Replace with your actual OpenWeatherMap API key
    private lateinit var weatherIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congratulations)

        weatherIcon = findViewById(R.id.weatherIcon)
        weatherIcon.visibility = View.VISIBLE

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

        // Weather icon logic
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getWeatherForCurrentLocation()

        val congratsText = findViewById<TextView>(R.id.congratsText)
        congratsText.text = "🎉 Great job!!"

        val stopAlarmButton = findViewById<Button>(R.id.stopAlarmButton)
        stopAlarmButton.setOnClickListener {
            stopService(Intent(this, AlarmService::class.java))
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
    }
}