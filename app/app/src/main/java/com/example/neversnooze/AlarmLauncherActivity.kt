package com.example.neversnooze

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class AlarmLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardLocked) {
                km.requestDismissKeyguard(this, null)
            }
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        val alarmHour = intent.getIntExtra("ALARM_HOUR", 0)
        val alarmMinute = intent.getIntExtra("ALARM_MINUTE", 0)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: ""
        val alarmSound = intent.getStringExtra("ALARM_SOUND") ?: "default_alarm"
        val challengeType = intent.getStringExtra("ALARM_CHALLENGE_TYPE") ?: "Button"

        val challengeActivity = when (challengeType.lowercase()) {
            "math" -> MathActivity::class.java
            "shake" -> ShakingActivity::class.java
            "object" -> ObjectPromptActivity::class.java
            else -> ButtonChallengeActivity::class.java
        }

        val challengeIntent = Intent(this, challengeActivity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", alarmHour)
            putExtra("ALARM_MINUTE", alarmMinute)
            putExtra("ALARM_LABEL", alarmLabel)
            putExtra("ALARM_SOUND", alarmSound)
            putExtra("ALARM_CHALLENGE_TYPE", challengeType)
        }

        startActivity(challengeIntent)
        finish()
    }
}
