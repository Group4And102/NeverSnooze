package com.example.neversnooze

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import android.widget.Spinner

class AlarmCreateActivity : AppCompatActivity() {

    // Day buttons
    private lateinit var btnSun: MaterialButton
    private lateinit var btnMon: MaterialButton
    private lateinit var btnTue: MaterialButton
    private lateinit var btnWed: MaterialButton
    private lateinit var btnThu: MaterialButton
    private lateinit var btnFri: MaterialButton
    private lateinit var btnSat: MaterialButton

    // Selected days tracker (index 0 = Sunday)
    private val selectedDays = BooleanArray(7) { false }

    // Database helper
    private lateinit var dbHelper: AlarmDatabaseHelper

    private var selectedSoundLabel: String = "Chimes"
    private var selectedSoundFile: String = "chimes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_create)


        // Set background color based on theme
        val rootLayout = findViewById<View>(R.id.main)
        val isDarkTheme = getSharedPreferences("app_preferences", MODE_PRIVATE)
            .getBoolean("dark_theme", true)
        if (isDarkTheme) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.alarm_create_background_dark))
            window.statusBarColor = ContextCompat.getColor(this, R.color.alarm_create_background_dark)
        } else {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.bgcreate))
            window.statusBarColor = ContextCompat.getColor(this, R.color.bgcreate)
        }

        val activitySpinner = findViewById<Spinner>(R.id.activitySpinner)


        // Initialize database helper
        dbHelper = AlarmDatabaseHelper(this)

        // Initialize views
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        if (isDarkTheme) {
            // Optionally, set dark style if needed (default may be fine)
        } else {
            // For light mode, set a light style if needed
            // You may need to use AppCompatDelegate or set a style programmatically if you want a custom look
            // But most devices will use the system's default light TimePicker
        }
        val alarmLabel = findViewById<EditText>(R.id.alarmLabel)
        val soundText = findViewById<TextView>(R.id.selectSound)
        val createButton = findViewById<MaterialButton>(R.id.createAlarmButton)
        val cancelButton = findViewById<TextView>(R.id.cancelButton)

        soundText.text = selectedSoundLabel

        // Set up cancel button click listener
        cancelButton.setOnClickListener {
            finish()
        }

        // Initialize day buttons and set click listeners
        initDayButtons()

        // Set up sound selection
        soundText.setOnClickListener {
            val soundDialog = SoundSelectorDialog.newInstance()

            soundDialog.setOnSoundSelectedListener { (label, fileName) ->
                selectedSoundLabel = label
                selectedSoundFile = fileName
                soundText.text = label
            }

            soundDialog.show(supportFragmentManager, "SoundSelector")
        }

        // Create alarm button click listener
        createButton.setOnClickListener {
            saveAlarm(
                hour = timePicker.hour,
                minute = timePicker.minute,
                label = alarmLabel.text.toString(),
                sound = selectedSoundFile
            )
        }
    }

    private fun initDayButtons() {
        btnSun = findViewById(R.id.btnSun)
        btnMon = findViewById(R.id.btnMon)
        btnTue = findViewById(R.id.btnTue)
        btnWed = findViewById(R.id.btnWed)
        btnThu = findViewById(R.id.btnThu)
        btnFri = findViewById(R.id.btnFri)
        btnSat = findViewById(R.id.btnSat)

        setupDayButtonListener(btnSun, 0)
        setupDayButtonListener(btnMon, 1)
        setupDayButtonListener(btnTue, 2)
        setupDayButtonListener(btnWed, 3)
        setupDayButtonListener(btnThu, 4)
        setupDayButtonListener(btnFri, 5)
        setupDayButtonListener(btnSat, 6)
    }

    private fun setupDayButtonListener(button: MaterialButton, dayIndex: Int) {
        button.setOnClickListener {
            selectedDays[dayIndex] = !selectedDays[dayIndex]
            updateButtonAppearance(button, selectedDays[dayIndex])
        }
    }

    private fun updateButtonAppearance(button: MaterialButton, isSelected: Boolean) {
        if (isSelected) {
            button.alpha = 1.0f
            button.backgroundTintList = getColorStateList(R.color.purple_500)
        } else {
            button.alpha = 0.7f
            button.backgroundTintList = getColorStateList(R.color.dark_gray)
        }
    }

    private fun saveAlarm(hour: Int, minute: Int, label: String, sound: String) {
        val daysString = selectedDays.joinToString(",") { if (it) "1" else "0" }

        if (!isValidAlarm(daysString)) {
            Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show()
            return
        }

        val activitySpinner = findViewById<Spinner>(R.id.activitySpinner)
        val challenge = activitySpinner.selectedItem.toString()
        Log.d(TAG, "Saving alarm with challengeType = \"$challenge\"")

        val values = ContentValues().apply {
            put(AlarmContract.AlarmEntry.COLUMN_HOUR, hour)
            put(AlarmContract.AlarmEntry.COLUMN_MINUTE, minute)
            put(AlarmContract.AlarmEntry.COLUMN_DAYS, daysString)
            put(AlarmContract.AlarmEntry.COLUMN_LABEL, label)
            put(AlarmContract.AlarmEntry.COLUMN_SOUND, sound)
            put(AlarmContract.AlarmEntry.COLUMN_ENABLED, 1)
            put(AlarmContract.AlarmEntry.COLUMN_CHALLENGE_TYPE, challenge)
        }

        val db = dbHelper.writableDatabase
        val newRowId = db.insert(AlarmContract.AlarmEntry.TABLE_NAME, null, values)

        if (newRowId != -1L) {
            Toast.makeText(this, "Alarm created successfully", Toast.LENGTH_SHORT).show()
            val savedAlarm = dbHelper.getAlarmById(newRowId)
            if (savedAlarm != null) {
                AlarmScheduler.scheduleAlarm(this, savedAlarm)
            }
            finish()
        } else {
            Toast.makeText(this, "Error creating alarm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidAlarm(daysString: String): Boolean {
        return daysString.contains("1")
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
