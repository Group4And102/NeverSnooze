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

        val extraSound     = intent.getStringExtra("ALARM_SOUND")
        val extraChallenge = intent.getStringExtra("ALARM_CHALLENGE_TYPE")

        val dbHelper = AlarmDatabaseHelper(context)
        val alarmRow = dbHelper.getAlarmById(alarmId)

        val alarmSound = extraSound ?: alarmRow?.sound ?: "chimes"
        val alarmChallengeType = extraChallenge
            ?: alarmRow?.challengeType
            ?: context.getString(R.string.challenge_tap_button)


        Log.d(TAG, "Alarm details: ID=$alarmId, Time=$alarmHour:$alarmMinute, Label=$alarmLabel, Sound=$alarmSound, Challenge=$alarmChallengeType")

        // Start the foreground service to play the alarm sound
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", alarmHour)
            putExtra("ALARM_MINUTE", alarmMinute)
            putExtra("ALARM_LABEL", alarmLabel)
            putExtra("ALARM_SOUND", alarmSound)
            putExtra("ALARM_CHALLENGE_TYPE", alarmChallengeType) // (use actual variable)
        }

        // Starting the service based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}