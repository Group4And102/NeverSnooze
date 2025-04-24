package com.example.neversnooze

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private var isDarkTheme = true // Starting with dark theme as default since navy_blue is already used
    private lateinit var recyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var dbHelper: AlarmDatabaseHelper
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initializing dbHelper
        dbHelper = AlarmDatabaseHelper(this)

        // Set up recycler view
        recyclerView = findViewById(R.id.alarmRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list
        alarmAdapter = AlarmAdapter(emptyList())
        recyclerView.adapter = alarmAdapter

        // Load theme preference
        val sharedPrefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
        isDarkTheme = sharedPrefs.getBoolean("dark_theme", true) // Default to dark theme

        // Apply current theme
        applyTheme(isDarkTheme)

        // Setup RecyclerView

        // Set up addButton click listener
        val addButton = findViewById<AppCompatImageButton>(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, AlarmCreateActivity::class.java)
            startActivity(intent)
        }

        // Set up theme toggle button
        val themeToggleButton = findViewById<AppCompatImageButton>(R.id.themeToggleButton)
        themeToggleButton.setOnClickListener {
            isDarkTheme = !isDarkTheme
            applyTheme(isDarkTheme)

            // Save theme preference
            sharedPrefs.edit().putBoolean("dark_theme", isDarkTheme).apply()
        }
    }
    override fun onResume() {
        super.onResume()
        loadAlarms()
    }
    private fun loadAlarms() {
        val alarms = queryAlarms()
        alarmAdapter.updateAlarms(alarms)

        // Show empty view if there are no alarms
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

        // Define projection (columns to fetch)
        val projection = arrayOf(
            AlarmContract.AlarmEntry.COLUMN_ID,
            AlarmContract.AlarmEntry.COLUMN_HOUR,
            AlarmContract.AlarmEntry.COLUMN_MINUTE,
            AlarmContract.AlarmEntry.COLUMN_DAYS,
            AlarmContract.AlarmEntry.COLUMN_LABEL,
            AlarmContract.AlarmEntry.COLUMN_SOUND,
            AlarmContract.AlarmEntry.COLUMN_ENABLED
        )

        // Sort by time
        val sortOrder = "${AlarmContract.AlarmEntry.COLUMN_HOUR} ASC, " +
                "${AlarmContract.AlarmEntry.COLUMN_MINUTE} ASC"

        val cursor = db.query(
            AlarmContract.AlarmEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOrder
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_ID))
                val hour = getInt(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_HOUR))
                val minute = getInt(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_MINUTE))
                val daysString = getString(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_DAYS))
                val label = getString(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_LABEL))
                val sound = getString(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_SOUND))
                val enabled = getInt(getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_ENABLED)) == 1

                val alarm = Alarm(
                    id = id,
                    hour = hour,
                    minute = minute,
                    days = Alarm.daysFromString(daysString),
                    label = label ?: "",
                    sound = sound,
                    enabled = enabled
                )

                alarms.add(alarm)
            }
        }

        cursor.close()
        return alarms
    }
    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }


    @SuppressLint("WrongViewCast")
    private fun applyTheme(isDark: Boolean) {
        // Get direct references to the views we want to modify
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootLayout) // Make sure to add this ID to your layout XML
        val alarmContainer = findViewById<ConstraintLayout>(R.id.alarmContainer)
        val buttonBar = findViewById<LinearLayout>(R.id.buttonBar)
        val titleText = findViewById<AppCompatTextView>(R.id.titleText)
        val themeToggleButton = findViewById<AppCompatImageButton>(R.id.themeToggleButton)
        val addButton = findViewById<AppCompatImageButton>(R.id.addButton)
        val trashButton = findViewById<AppCompatImageButton>(R.id.trashButton)

        if (isDark) {
            // Dark theme
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.navy_blue))
            alarmContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.navy_blue))
            titleText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            themeToggleButton.setImageResource(R.drawable.baseline_dark_mode_24)

            // Update button tints for dark theme
            val buttonTint = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
            addButton.imageTintList = buttonTint
            trashButton.imageTintList = buttonTint
            themeToggleButton.imageTintList = buttonTint
        } else {
            // Light theme
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            alarmContainer.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            titleText.setTextColor(ContextCompat.getColor(this, R.color.navy_blue))
            themeToggleButton.setImageResource(R.drawable.baseline_sunny_24)

            // Update button tints for light theme
            val buttonTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navy_blue))
            addButton.imageTintList = buttonTint
            trashButton.imageTintList = buttonTint
            themeToggleButton.imageTintList = buttonTint
        }
    }
}