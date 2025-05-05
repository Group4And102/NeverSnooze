package com.example.neversnooze

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

/**
 * Utility class for scheduling alarms with AlarmManager
 */
object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    /**
     * Schedule an alarm with the system AlarmManager
     */
    fun scheduleAlarm(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancel any existing alarm to avoid duplication
        cancelAlarm(context, alarm)
        
        // If the alarm is not enabled, just cancel any existing alarms
        if (!alarm.enabled) {
            return
        }

        // Check if this is a repeating alarm (at least one day selected)
        val isRepeating = alarm.days.any { it }

        if (isRepeating) {
            // Schedule separate alarms for each selected day
            alarm.days.forEachIndexed { dayOfWeek, isSelected ->
                if (isSelected) {
                    scheduleAlarmForDay(context, alarm, dayOfWeek, alarmManager)
                }
            }
        } else {
            // Schedule a one-time alarm
            scheduleOneTimeAlarm(context, alarm, alarmManager)
        }
    }

    /**
     * Schedule alarm for a specific day of the week
     */
    private fun scheduleAlarmForDay(
        context: Context,
        alarm: Alarm,
        dayOfWeek: Int,
        alarmManager: AlarmManager
    ) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()

            set(Calendar.DAY_OF_WEEK, dayOfWeek + 1)

            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val requestCode = (alarm.id.toInt() * 10) + dayOfWeek
        val intent = createAlarmIntent(context, alarm, requestCode)

        Log.d(TAG, "Scheduling repeating alarm for day $dayOfWeek at ${alarm.hour}:${alarm.minute}, time=${calendar.timeInMillis}")
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            intent
        )
    }

    /**
     * Schedule a one-time (non-repeating) alarm
     */
    private fun scheduleOneTimeAlarm(
        context: Context,
        alarm: Alarm,
        alarmManager: AlarmManager
    ) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time is in the past, add a day
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val requestCode = alarm.id.toInt()
        val intent = createAlarmIntent(context, alarm, requestCode)

        Log.d(TAG, "Scheduling one-time alarm at ${alarm.hour}:${alarm.minute}, time=${calendar.timeInMillis}")

        // Set exact alarm with highest priority
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    intent
                )
            } else {
                // If we can't set exact alarms, try to set it as close as possible
                val currentTime = System.currentTimeMillis()
                if (calendar.timeInMillis - currentTime <= 60 * 1000) { // If within 1 minute
                    // Execute immediately
                    val broadcastIntent = Intent(context, AlarmReceiver::class.java).apply {
                        action = "com.example.neversnooze.ALARM_TRIGGERED"
                        putExtra("ALARM_ID", alarm.id)
                        putExtra("ALARM_HOUR", alarm.hour)
                        putExtra("ALARM_MINUTE", alarm.minute)
                        putExtra("ALARM_LABEL", alarm.label)
                        putExtra("ALARM_SOUND", alarm.sound)
                    }
                    context.sendBroadcast(broadcastIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        intent
                    )
                }
            }
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                intent
            )
        }
    }

    /**
     * Create the PendingIntent for an alarm
     */
    private fun createAlarmIntent(context: Context, alarm: Alarm, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.neversnooze.ALARM_TRIGGERED"
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_HOUR", alarm.hour)
            putExtra("ALARM_MINUTE", alarm.minute)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_SOUND", alarm.sound)
        }

        Log.d(TAG, "Creating alarm intent with action: ${intent.action}, requestCode: $requestCode")

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Cancel all pending intents for an alarm
     */
    fun cancelAlarm(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel the one-time alarm
        val oneTimeIntent = Intent(context, AlarmReceiver::class.java)
        val oneTimePendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            oneTimeIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        oneTimePendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }

        // Cancel all repeating alarms (one for each day)
        for (i in 0..6) {
            val requestCode = (alarm.id.toInt() * 10) + i
            val repeatingIntent = Intent(context, AlarmReceiver::class.java)
            val repeatingPendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                repeatingIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            repeatingPendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }

        Log.d(TAG, "Cancelled all alarms for ID ${alarm.id}")
    }
}