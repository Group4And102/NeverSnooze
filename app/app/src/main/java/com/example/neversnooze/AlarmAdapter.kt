package com.example.neversnooze

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private var alarms: List<Alarm>,
    private val onAlarmClick: (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private var isDeleteMode = false

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.timeText)
        val daysText: TextView = itemView.findViewById(R.id.daysText)
        val labelText: TextView = itemView.findViewById(R.id.labelText)
        val enableSwitch: Switch = itemView.findViewById(R.id.enableSwitch)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        val challengeText: TextView = itemView.findViewById(R.id.challengeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]

        // Set the time
        holder.timeText.text = alarm.getFormattedTime()

        // Set the days
        holder.daysText.text = alarm.getFormattedDays()

        // Set the label (if any)
        if (alarm.label.isNotEmpty()) {
            holder.labelText.visibility = View.VISIBLE
            holder.labelText.text = alarm.label
        } else {
            holder.labelText.visibility = View.GONE
        }

        holder.challengeText.text = "Challenge: ${alarm.challengeType}"

        // Set the enabled status
        holder.enableSwitch.isChecked = alarm.enabled

        // Set listener for switch
        holder.enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Update the alarm object with the new enabled state
            val updatedAlarm = alarm.copy(enabled = isChecked)

            // Update the alarm in the database
            val dbHelper = AlarmDatabaseHelper(holder.itemView.context)
            dbHelper.updateAlarm(updatedAlarm)

            // Show a toast for feedback
            Toast.makeText(holder.itemView.context,
                "Alarm ${if (isChecked) "enabled" else "disabled"}",
                Toast.LENGTH_SHORT).show()

            // Update the alarm list in the adapter and notify RecyclerView
            val updatedAlarms = alarms.toMutableList().apply { set(position, updatedAlarm) }
            updateAlarms(updatedAlarms)

            // Schedule or cancel the alarm based on the enabled state
            if (isChecked) {
                AlarmScheduler.scheduleAlarm(holder.itemView.context, updatedAlarm)
            } else {
                AlarmScheduler.cancelAlarm(holder.itemView.context, updatedAlarm)
            }
        }

        // Handle delete mode
        holder.deleteButton.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
        holder.deleteButton.setOnClickListener {
            onAlarmClick(alarm)
        }

        // Handle item click
        holder.itemView.setOnClickListener {
            if (isDeleteMode) {
                onAlarmClick(alarm)
            }
        }
    }

    override fun getItemCount() = alarms.size

    fun updateAlarms(newAlarms: List<Alarm>) {
        this.alarms = newAlarms
        notifyDataSetChanged()
    }

    fun setDeleteMode(isDeleteMode: Boolean) {
        this.isDeleteMode = isDeleteMode
        notifyDataSetChanged()
    }
}