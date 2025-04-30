package com.example.neversnooze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * BroadcastReceiver that gets triggered when an alarm time is reached
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "AlarmReceiver received intent with action: ${intent.action}")
        
        if (intent.action != "com.example.neversnooze.ALARM_TRIGGERED") {
            Log.w(TAG, "Received intent with unexpected action: ${intent.action}")
            return
        }

        Log.d(TAG, "Alarm triggered!")

        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        val alarmHour = intent.getIntExtra("ALARM_HOUR", 0)
        val alarmMinute = intent.getIntExtra("ALARM_MINUTE", 0)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: ""
        val alarmSound = intent.getStringExtra("ALARM_SOUND") ?: "default_alarm"

        Log.d(TAG, "Alarm details: ID=$alarmId, Time=$alarmHour:$alarmMinute, Label=$alarmLabel, Sound=$alarmSound")

        // Launch the alarm ringing activity
        val ringingIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", alarmHour)
            putExtra("ALARM_MINUTE", alarmMinute)
            putExtra("ALARM_LABEL", alarmLabel)
            putExtra("ALARM_SOUND", alarmSound)
        }
        context.startActivity(ringingIntent)

        // Start the foreground service to play the alarm sound
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", alarmHour)
            putExtra("ALARM_MINUTE", alarmMinute)
            putExtra("ALARM_LABEL", alarmLabel)
            putExtra("ALARM_SOUND", alarmSound)
        }

        // Starting the service based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}