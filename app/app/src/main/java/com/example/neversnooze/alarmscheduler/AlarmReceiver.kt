package com.example.neversnooze.alarmscheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast
import com.example.neversnooze.R

/* handles the action when an alarm is active
*  for now sends a toast and plays an alarm sound
* TODO send the user to the alarm ringing screen
*  it should force them out of any app they are currently in
*  or we could just overlay it on top of the app */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("ALARM")

        Toast.makeText(context, label ?: "Alarm", Toast.LENGTH_LONG).show()

        val mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound_1)
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }
}
