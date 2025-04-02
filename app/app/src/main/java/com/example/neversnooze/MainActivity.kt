package com.example.neversnooze

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Sample data
        val sampleAlarms = listOf(
            Alarm("9:27 AM", "Never Snooze", "Daily"),
            Alarm("10:00 AM", "Class", "Weekdays"),
            Alarm("7:00 PM", "Throw out Trash", "Daily"),
            Alarm("11:30 PM", "Sleep", "Daily")
        )

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.alarmRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AlarmAdapter(sampleAlarms)

        // Add divider decoration to RecyclerView
        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.divider)?.let { drawable ->
            divider.setDrawable(drawable)
        }
        recyclerView.addItemDecoration(divider)

        // Change R.id.main to your root ConstraintLayout's ID
        // Edge-to-edge handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.buttonBar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
