package com.example.neversnooze

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private var alarms: List<Alarm>,
    private val onAlarmClick: (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    companion object {
        private var activeToast: Toast? = null
    }

    private var isDeleteMode = false
    private var isDarkTheme = true

    fun updateTheme(isDark: Boolean) {
        isDarkTheme = isDark
        notifyDataSetChanged()
    }

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
        val context = holder.itemView.context

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


        if (isDarkTheme) {
            // DARK MODE: Use dark theme colors
            holder.timeText.setTextColor(context.getColor(R.color.white))
            holder.daysText.setTextColor(context.getColor(R.color.white))
            holder.labelText.setTextColor(context.getColor(R.color.white))
            holder.itemView.setBackgroundColor(context.getColor(R.color.card_background))
            holder.enableSwitch.thumbTintList = null
            holder.enableSwitch.trackTintList = null
            holder.challengeText.setTextColor(context.getColor(R.color.white))
        } else {
            // LIGHT MODE: Use light theme colors
            holder.timeText.setTextColor(context.getColor(android.R.color.black))
            holder.daysText.setTextColor(context.getColor(android.R.color.black))
            holder.labelText.setTextColor(context.getColor(android.R.color.black))
            holder.itemView.setBackgroundColor(context.getColor(R.color.surface))
            holder.enableSwitch.thumbTintList = android.content.res.ColorStateList.valueOf(context.getColor(R.color.black))
            holder.enableSwitch.trackTintList = android.content.res.ColorStateList.valueOf(context.getColor(R.color.button_color))
            holder.challengeText.setTextColor(context.getColor(android.R.color.black))
        }

        holder.challengeText.text = "Challenge: ${alarm.challengeType}"


        holder.enableSwitch.setOnCheckedChangeListener(null)
        holder.enableSwitch.isChecked = alarm.enabled

        holder.enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnCheckedChangeListener

            val updated = alarms[pos].copy(enabled = isChecked)
            alarms = alarms.toMutableList().apply { set(pos, updated) }
            AlarmDatabaseHelper(context).updateAlarm(updated)

            if (isChecked) {
                AlarmScheduler.scheduleAlarm(context, updated)
            } else {
                AlarmScheduler.cancelAlarm(context, updated)
            }

            val msg = if (isChecked) {
                val now = java.util.Calendar.getInstance()
                val next = (now.clone() as java.util.Calendar).apply {
                    set(java.util.Calendar.HOUR_OF_DAY, updated.hour)
                    set(java.util.Calendar.MINUTE, updated.minute)
                    set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
                    if (timeInMillis <= now.timeInMillis) add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
                val diff = next.timeInMillis - now.timeInMillis
                val h = diff / 3_600_000
                val m = (diff / 60_000) % 60
                "Alarm enabled – rings in ${h}h ${m}m"
            } else "Alarm disabled"

            activeToast?.cancel()
            activeToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
        }


        // Handle delete mode
        holder.deleteButton.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
        holder.deleteButton.setColorFilter(
            if (isDarkTheme)
                context.getColor(android.R.color.holo_red_light)
            else
                context.getColor(android.R.color.holo_red_light)
        )
        holder.deleteButton.setOnClickListener {
            onAlarmClick(alarm)
        }

        // Handle item click
        holder.itemView.setOnClickListener {
            if (isDeleteMode) {
                onAlarmClick(alarm)
            }
        }
        // Handle test button click
        holder.itemView.findViewById<ImageButton>(R.id.testButton).setOnClickListener {
            val ctx = holder.itemView.context

            val testIntent = Intent(ctx, AlarmReceiver::class.java).apply {
                action = "com.example.neversnooze.ALARM_TRIGGERED"
                putExtra("ALARM_ID",     alarm.id)
                putExtra("ALARM_HOUR",   alarm.hour)
                putExtra("ALARM_MINUTE", alarm.minute)
                putExtra("ALARM_LABEL",  alarm.label)
                putExtra("ALARM_SOUND",  alarm.sound)
                putExtra("ALARM_CHALLENGE_TYPE", alarm.challengeType)
            }
            ctx.sendBroadcast(testIntent)
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