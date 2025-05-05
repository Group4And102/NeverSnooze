package com.example.neversnooze

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * Full-screen overlay that steals focus even when the phone is unlocked.
 * It immediately forwards to AlarmLauncherActivity (normal challenge picker).
 */
class OverlayChallengeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the window type for the overlay based on the Android version.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            @Suppress("DEPRECATION")
            window.setType(WindowManager.LayoutParams.TYPE_PHONE)
        }

        // Ensure the overlay has focus.
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        // Make the overlay full-screen.
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        // Hide system UI elements
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        // Forward to AlarmLauncherActivity
        val extras = intent.extras
        startActivity(
            Intent(this, AlarmLauncherActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            or Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                extras?.let { putExtras(it) }
            }
        )
        finish()
    }
}