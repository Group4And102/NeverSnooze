package com.example.neversnooze

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ShakingActivity : AppCompatActivity(), SensorEventListener {

    var sensor: Sensor?=null
    var sensorManager: SensorManager?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shaking)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.shakeText)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume(){
        super.onResume()
        sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }

    var xold = 0.0
    var yold = 0.0
    var zold = 0.0
    var oldtime:Long = 0
    var shakeCount = 0
    var lastShakeTime = 0L
    var activationThreshold = 13  // How many "small shakes" needed
    var lowShakeThreshold = 5.0  // Less sensitive to require less force

    override fun onSensorChanged(event: SensorEvent?) {
        val x = event!!.values[0]
        val y = event!!.values[1]
        val z = event!!.values[2]

        val currentTime = System.currentTimeMillis()

        if ((currentTime - oldtime) > 100) {
            val timeDiff = currentTime - oldtime
            oldtime = currentTime

            val speed = Math.abs(x + y + z - xold - yold - zold) / timeDiff * 1000

            xold = x.toDouble()
            yold = y.toDouble()
            zold = z.toDouble()

            if (speed > lowShakeThreshold) {
                if (currentTime - lastShakeTime > 500) {
                    // Too slow, reset
                    shakeCount = 0
                }
                shakeCount++
                lastShakeTime = currentTime
            }

            if (shakeCount >= activationThreshold) {
                val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    v.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    v.vibrate(500)
                }

                runOnUiThread {
                    showChallengeDialog()
                }
                sensorManager!!.unregisterListener(this) // prevent more shakes while dialog is open
            }
        }
    }

    private fun showChallengeDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Challenge Achieved")
            .setMessage("You successfully completed the shaking challenge.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                finish() // Close this activity
            }
            .create()

        dialog.show()
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}