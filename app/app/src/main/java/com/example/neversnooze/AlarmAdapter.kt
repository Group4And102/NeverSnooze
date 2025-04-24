package com.example.neversnooze

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(private var alarms: List<Alarm>) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText: TextView = view.findViewById(R.id.textTime)
        val daysText: TextView = view.findViewById(R.id.textDays)
        val labelText: TextView = view.findViewById(R.id.textLabel)
        val enableSwitch: Switch = view.findViewById(R.id.switchEnable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm_card, parent, false)
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

        // Set the enabled status
        holder.enableSwitch.isChecked = alarm.enabled

        // Set listener for switch (not implemented yet)
        holder.enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            // In a real app, you would update the database here
            // For now, just show a toast
            Toast.makeText(holder.itemView.context,
                 "Alarm ${if (isChecked) "enabled" else "disabled"}",
                 Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = alarms.size

    fun updateAlarms(newAlarms: List<Alarm>) {
        this.alarms = newAlarms
        notifyDataSetChanged()
    }
}