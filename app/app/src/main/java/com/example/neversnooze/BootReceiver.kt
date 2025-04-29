package com.example.neversnooze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver that handles device boot to restore all enabled alarms
 */
class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, restoring alarms")
            // Run database operation on a background thread
            Thread {
                val dbHelper = AlarmDatabaseHelper(context)
                val alarms = dbHelper.getAlarms()

                // Reschedule all enabled alarms
                for (alarm in alarms) {
                    if (alarm.enabled) {
                        AlarmScheduler.scheduleAlarm(context, alarm)
                        Log.d(TAG, "Restored alarm ${alarm.id} for ${alarm.hour}:${alarm.minute}")
                    }
                }
            }.start()
        }
    }
}