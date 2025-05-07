package com.example.neversnooze

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import android.provider.Settings

/**
 * Foreground service that plays the alarm sound
 */
class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var alarmId: Long = -1

    companion object {
        private const val TAG = "AlarmService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "NeverSnoozeAlarmChannel"
        var activeAlarmExtras : Bundle? = null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AlarmService created")

        // Initialize vibrator
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Acquire wake lock to keep device awake
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NeverSnooze::AlarmWakeLock"
        )
        wakeLock?.acquire(10*60*1000L /*10 minutes*/)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AlarmService started")

        // Create notification channel (required for Android 8.0+)
        createNotificationChannel()

        // Extract alarm details from intent
        alarmId = intent?.getLongExtra("ALARM_ID", -1) ?: -1
        val hour           = intent?.getIntExtra("ALARM_HOUR", 0) ?: 0
        val minute         = intent?.getIntExtra("ALARM_MINUTE", 0) ?: 0
        val label          = intent?.getStringExtra("ALARM_LABEL") ?: ""
        val sound          = intent?.getStringExtra("ALARM_SOUND") ?: "default_alarm"
        val challengeType  = intent?.getStringExtra("ALARM_CHALLENGE_TYPE") ?: "Button"
        val alarmExtras    = intent?.extras        // keep the bundle

        // Build & post the foreground notification (includes FSI for locked screen)
        val notification = createNotification(hour, minute, label, sound, challengeType)
        startForeground(NOTIFICATION_ID, notification)

        // Determine current device state
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val pm = getSystemService(Context.POWER_SERVICE)   as PowerManager
        val unlockedAndInteractive = !km.isKeyguardLocked && pm.isInteractive

        /* continuous 500 ms on / 500 ms off */
        vibrator?.let { vib ->
            val pattern = longArrayOf(0, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(pattern, 0)
            }
        }
        if (unlockedAndInteractive) {
            if (Settings.canDrawOverlays(this)) {
                // Launch overlay so we still steal focus
                val overlay = Intent(this, OverlayChallengeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    alarmExtras?.let { putExtras(it) }
                }
                startActivity(overlay)
            } else {
                // Prompt user once to grant “Appear on top”
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }

        playAlarmSound(sound)
        activeAlarmExtras = intent?.extras
        return START_STICKY
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "NeverSnooze Alarm"
            val descriptionText = "Alarm notifications for NeverSnooze"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setBypassDnd(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(hour: Int, minute: Int, label: String, sound: String, challengeType: String): Notification {
        val notificationIntent = Intent(this, AlarmLauncherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", hour)
            putExtra("ALARM_MINUTE", minute)
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_SOUND", sound)
            putExtra("ALARM_CHALLENGE_TYPE", challengeType)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedTime = String.format("%02d:%02d", hour, minute)
        val title = if (label.isNotEmpty()) label else "Alarm"
        val challengeLabel = when (challengeType.lowercase()) {
            "math" -> "Math"
            "shake" -> "Shake"
            "object" -> "Object Detection"
            else -> "Button Press"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("$formattedTime - Complete challenge to stop alarm")
            .setSmallIcon(R.drawable.ic_notification_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .build()
    }

    private fun playAlarmSound(soundName: String) {
        try {
            // Release any existing MediaPlayer
            mediaPlayer?.release()

            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                val soundUri = if (soundName == "default_alarm") {
                    // Use default alarm sound
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                } else {
                    // Use selected sound from resources
                    Uri.parse("android.resource://${packageName}/raw/${soundName}")
                }

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                setDataSource(applicationContext, soundUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)

            // Fallback to default alarm sound if there's an error
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(
                        applicationContext,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    )
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing fallback alarm sound", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Release wake lock
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }

        // Clean up media player
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        activeAlarmExtras = null
        Log.d(TAG, "AlarmService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}