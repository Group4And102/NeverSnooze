package com.example.neversnooze

import android.content.ContentValues
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_create)

        // Initialize database helper
        dbHelper = AlarmDatabaseHelper(this)

        // Initialize views
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val alarmLabel = findViewById<EditText>(R.id.alarmLabel)
        val soundText = findViewById<TextView>(R.id.selectSound)
        val createButton = findViewById<MaterialButton>(R.id.createAlarmButton)

        // Initialize day buttons and set click listeners
        initDayButtons()

        // Set up sound selection
        soundText.setOnClickListener {
            val soundDialog = SoundSelectorDialog.newInstance()
            soundDialog.setOnSoundSelectedListener { selectedSound ->
                soundText.text = selectedSound
            }
            soundDialog.show(supportFragmentManager, "SoundSelector")
        }

        // Create alarm button click listener
        createButton.setOnClickListener {
            saveAlarm(
                hour = timePicker.hour,
                minute = timePicker.minute,
                label = alarmLabel.text.toString(),
                sound = soundText.text.toString()
            )
        }
    }

    private fun initDayButtons() {
        // Initialize buttons
        btnSun = findViewById(R.id.btnSun)
        btnMon = findViewById(R.id.btnMon)
        btnTue = findViewById(R.id.btnTue)
        btnWed = findViewById(R.id.btnWed)
        btnThu = findViewById(R.id.btnThu)
        btnFri = findViewById(R.id.btnFri)
        btnSat = findViewById(R.id.btnSat)

        // Set up click listeners for day buttons
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
            // Toggle selection
            selectedDays[dayIndex] = !selectedDays[dayIndex]

            // Update visual appearance
            updateButtonAppearance(button, selectedDays[dayIndex])
        }
    }

    private fun updateButtonAppearance(button: MaterialButton, isSelected: Boolean) {
        if (isSelected) {
            // Selected state
            button.alpha = 1.0f
            button.backgroundTintList = getColorStateList(R.color.purple_500)
        } else {
            // Unselected state
            button.alpha = 0.7f
            button.backgroundTintList = getColorStateList(R.color.dark_gray)
        }
    }

    private fun saveAlarm(hour: Int, minute: Int, label: String, sound: String) {
        // Convert selected days to a string format for storage
        // Format: "1,0,1,0,1,0,1" (1=selected, 0=not selected)
        val daysString = selectedDays.joinToString(",") { if (it) "1" else "0" }

        // Validate inputs
        if (!isValidAlarm(daysString)) {
            Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show()
            return
        }

        // Create values for database insertion
        val values = ContentValues().apply {
            put(AlarmContract.AlarmEntry.COLUMN_HOUR, hour)
            put(AlarmContract.AlarmEntry.COLUMN_MINUTE, minute)
            put(AlarmContract.AlarmEntry.COLUMN_DAYS, daysString)
            put(AlarmContract.AlarmEntry.COLUMN_LABEL, label)
            put(AlarmContract.AlarmEntry.COLUMN_SOUND, sound)
            put(AlarmContract.AlarmEntry.COLUMN_ENABLED, 1) // 1 = enabled by default
        }

        // Insert into database
        val db = dbHelper.writableDatabase
        val newRowId = db.insert(AlarmContract.AlarmEntry.TABLE_NAME, null, values)

        if (newRowId != -1L) {
            Toast.makeText(this, "Alarm created successfully", Toast.LENGTH_SHORT).show()

            // After saving to the database, get the saved alarm with its ID
            val savedAlarm = dbHelper.getAlarmById(newRowId)

            // Schedule the alarm with the system
            if (savedAlarm != null) {
                AlarmScheduler.scheduleAlarm(this, savedAlarm)
            }

            // Close this activity and return to the main activity
            finish()
        } else {
            Toast.makeText(this, "Error creating alarm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidAlarm(daysString: String): Boolean {
        // Check if at least one day is selected
        return daysString.contains("1")
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}