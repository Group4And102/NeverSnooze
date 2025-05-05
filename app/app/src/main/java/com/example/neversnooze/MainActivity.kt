package com.example.neversnooze

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private var isDarkTheme = true
    private lateinit var recyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var dbHelper: AlarmDatabaseHelper
    private lateinit var trashButton: AppCompatImageButton
    private lateinit var themeButton: AppCompatImageButton
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var alarmContainer: ConstraintLayout
    private lateinit var buttonBar: LinearLayout
    private lateinit var titleText: AppCompatTextView
    private var isDeleteMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //deleteDatabase("Alarms.db") // ⚠️ Only for one-time reset
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize views
        initializeViews()

        // Set up RecyclerView
        setupRecyclerView()

        // Load theme preference
        val sharedPrefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
        isDarkTheme = sharedPrefs.getBoolean("dark_theme", true)

        // Apply initial theme
        updateThemeUI()
        // Set up click listeners
        setupClickListeners(sharedPrefs)

        requestIgnoreBatteryOptimization()
        requestOverlayPermission()
    }

    private fun initializeViews() {
        rootLayout = findViewById(R.id.rootLayout)
        alarmContainer = findViewById(R.id.alarmContainer)
        buttonBar = findViewById(R.id.buttonBar)
        titleText = findViewById(R.id.titleText)
        themeButton = findViewById(R.id.themeToggleButton)
        trashButton = findViewById(R.id.trashButton)
        recyclerView = findViewById(R.id.alarmRecyclerView)
        dbHelper = AlarmDatabaseHelper(this)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        alarmAdapter = AlarmAdapter(emptyList()) { alarm ->
            if (isDeleteMode) {
                deleteAlarm(alarm)
            }
        }
        recyclerView.adapter = alarmAdapter
    }

    private fun setupClickListeners(sharedPrefs: android.content.SharedPreferences) {
        // Theme toggle button
        themeButton.setOnClickListener {
            isDarkTheme = !isDarkTheme
            sharedPrefs.edit().putBoolean("dark_theme", isDarkTheme).apply()
            updateThemeUI()
        }

        // Add button
        findViewById<AppCompatImageButton>(R.id.addButton).setOnClickListener {
            val intent = Intent(this, AlarmCreateActivity::class.java)
            startActivity(intent)
        }

        // Trash button
        trashButton.setOnClickListener {
            toggleDeleteMode()
        }
    }

    private fun updateThemeUI() {
        // Update theme button icon
        themeButton.setImageResource(
            if (isDarkTheme) R.drawable.baseline_dark_mode_24
            else R.drawable.baseline_sunny_24
        )

        // Update colors
        if (isDarkTheme) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.navy_blue))
            alarmContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.navy_blue))
            buttonBar.setBackgroundColor(ContextCompat.getColor(this, R.color.card_background))
            titleText.setTextColor(ContextCompat.getColor(this, R.color.white))

            val buttonTint = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
            themeButton.imageTintList = buttonTint
            findViewById<AppCompatImageButton>(R.id.addButton).imageTintList = buttonTint
            trashButton.imageTintList = buttonTint
        } else {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background))
            alarmContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.background))
            buttonBar.setBackgroundColor(ContextCompat.getColor(this, R.color.surface))
            titleText.setTextColor(ContextCompat.getColor(this, R.color.navy_blue))

            val buttonTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button_color))
            themeButton.imageTintList = buttonTint
            findViewById<AppCompatImageButton>(R.id.addButton).imageTintList = buttonTint
            trashButton.imageTintList = buttonTint
        }

        // Update adapter theme
        alarmAdapter.updateTheme(isDarkTheme)
    }

    override fun onResume() {
        super.onResume()
        loadAlarms()
    }

    private fun loadAlarms() {
        val alarms = queryAlarms()
        alarmAdapter.updateAlarms(alarms)

        val emptyView = findViewById<View>(R.id.emptyView)
        if (alarms.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun queryAlarms(): List<Alarm> {
        val alarms = mutableListOf<Alarm>()
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            AlarmContract.AlarmEntry.COLUMN_ID,
            AlarmContract.AlarmEntry.COLUMN_HOUR,
            AlarmContract.AlarmEntry.COLUMN_MINUTE,
            AlarmContract.AlarmEntry.COLUMN_DAYS,
            AlarmContract.AlarmEntry.COLUMN_LABEL,
            AlarmContract.AlarmEntry.COLUMN_SOUND,
            AlarmContract.AlarmEntry.COLUMN_ENABLED,
            AlarmContract.AlarmEntry.COLUMN_CHALLENGE_TYPE
        )

        val sortOrder = "${AlarmContract.AlarmEntry.COLUMN_HOUR} ASC, ${AlarmContract.AlarmEntry.COLUMN_MINUTE} ASC"

        val cursor = db.query(
            AlarmContract.AlarmEntry.TABLE_NAME,
            projection,
            null, null, null, null,
            sortOrder
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_ID))
                val hour = getInt(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_HOUR))
                val minute = getInt(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_MINUTE))
                val daysString = getString(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_DAYS))
                val label = getString(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_LABEL)) ?: ""
                val sound = getString(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_SOUND))
                val enabled = getInt(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_ENABLED)) == 1

                val alarm = Alarm(
                    id = id,
                    hour = hour,
                    minute = minute,
                    days = Alarm.daysFromString(daysString),
                    label = label,
                    sound = sound,
                    enabled = enabled,
                    challengeType = cursor.getString(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_CHALLENGE_TYPE))
                )

                alarms.add(alarm)
            }
        }

        cursor.close()
        return alarms
    }
    private fun requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val pm = getSystemService(PowerManager::class.java)
        val exempt = pm.isIgnoringBatteryOptimizations(packageName)

        if (!exempt) {
            showPrompt(
                title = "Allow uninterrupted alarms?",
                message = "To make sure alarms ring on time, NeverSnooze needs to be excluded from battery optimizations.",
                intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (Settings.canDrawOverlays(this)) return

        showPrompt(
            title = "Show alarm over other apps?",
            message = "To display the alarm screen even when you're using another app, NeverSnooze needs the “Appear on top” permission.",
            intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        )
    }

    private fun showPrompt(title: String, message: String, intent: Intent) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Allow") { _, _ ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun onAlarmEnabledChanged(alarm: Alarm, isEnabled: Boolean) {
        val dbHelper = AlarmDatabaseHelper(this)

        val updatedAlarm = alarm.copy(enabled = isEnabled)
        dbHelper.updateAlarm(updatedAlarm)

        if (isEnabled) {
            AlarmScheduler.scheduleAlarm(this, updatedAlarm)
        } else {
            AlarmScheduler.cancelAlarm(this, updatedAlarm)
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    private fun toggleDeleteMode() {
        isDeleteMode = !isDeleteMode
        alarmAdapter.setDeleteMode(isDeleteMode)
        
        // Update trash button appearance
        if (isDeleteMode) {
            trashButton.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_light))
        } else {
            trashButton.setColorFilter(
                if (isDarkTheme) ContextCompat.getColor(this, android.R.color.white)
                else ContextCompat.getColor(this, R.color.navy_blue)
            )
        }
    }

    private fun deleteAlarm(alarm: Alarm) {
        // Cancel the alarm first
        AlarmScheduler.cancelAlarm(this, alarm)

        // Delete from database
        val db = dbHelper.writableDatabase
        db.delete(
            AlarmContract.AlarmEntry.TABLE_NAME,
            "${AlarmContract.AlarmEntry.COLUMN_ID} = ?",
            arrayOf(alarm.id.toString())
        )

        // Reload alarms
        loadAlarms()
    }
}
