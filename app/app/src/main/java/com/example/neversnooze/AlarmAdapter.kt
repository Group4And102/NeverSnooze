package com.example.neversnooze

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// AlarmAdapter.kt
class AlarmAdapter(private val alarms: List<Alarm>) :
    RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.timeText)
        val taskText: TextView = itemView.findViewById(R.id.taskText)
        val scheduleText: TextView = itemView.findViewById(R.id.scheduleText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm_card, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.timeText.text = alarm.time
        holder.taskText.text = alarm.task
        holder.scheduleText.text = alarm.schedule
    }

    override fun getItemCount() = alarms.size
}