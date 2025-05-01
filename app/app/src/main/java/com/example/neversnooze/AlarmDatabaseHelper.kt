package com.example.neversnooze

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Database helper for the alarm database
 */
class AlarmDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "Alarms.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create the alarms table
        android.util.Log.d("DB", "onCreate called — creating alarms table") // ✅ Add this log
        db.execSQL(AlarmContract.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply discard the data and start over
        db.execSQL(AlarmContract.SQL_DELETE_TABLE)
        onCreate(db)
    }

    fun updateAlarm(alarm: Alarm) {
        val db = writableDatabase
        val values = android.content.ContentValues().apply {
            put(AlarmContract.AlarmEntry.COLUMN_HOUR, alarm.hour)
            put(AlarmContract.AlarmEntry.COLUMN_MINUTE, alarm.minute)
            put(AlarmContract.AlarmEntry.COLUMN_DAYS, alarm.daysToString())
            put(AlarmContract.AlarmEntry.COLUMN_LABEL, alarm.label)
            put(AlarmContract.AlarmEntry.COLUMN_SOUND, alarm.sound)
            put(AlarmContract.AlarmEntry.COLUMN_ENABLED, if (alarm.enabled) 1 else 0) // Store enabled state as 1 (true) or 0 (false)
            put(AlarmContract.AlarmEntry.COLUMN_CHALLENGE_TYPE, alarm.challengeType)
        }

        // Update the alarm in the database
        db.update(
            AlarmContract.AlarmEntry.TABLE_NAME,
            values,
            "${AlarmContract.AlarmEntry.COLUMN_ID} = ?",
            arrayOf(alarm.id.toString())
        )
    }

    /**
     * Get all alarms from the database
     * @return List of all alarms stored in the database
     */
    fun getAlarms(): List<Alarm> {
        val alarms = mutableListOf<Alarm>()
        val db = this.readableDatabase
        val cursor = db.query(
            AlarmContract.AlarmEntry.TABLE_NAME, // The table to query
            null, // Get all columns
            null, // No WHERE clause
            null, // No WHERE arguments
            null, // No GROUP BY
            null, // No HAVING
            null  // Default sort order
        )

        // Iterate through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_ID))
                val hour = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_HOUR))
                val minute = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_MINUTE))
                val daysString = cursor.getString(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_DAYS))
                val label = cursor.getString(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_LABEL))
                val sound = cursor.getString(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_SOUND))
                val enabled = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_ENABLED)) == 1
                val challengeType = cursor.getString(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_CHALLENGE_TYPE))

                // Convert daysString to a List<Boolean>
                val days = Alarm.daysFromString(daysString)

                // Create and add the alarm to our list
                alarms.add(Alarm(id, hour, minute, days, label, sound, enabled, challengeType))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return alarms
    }

    /**
     * Get a single alarm by its ID
     * @param id The ID of the alarm to retrieve
     * @return The Alarm object or null if not found
     */
    fun getAlarmById(id: Long): Alarm? {
        val db = this.readableDatabase
        val cursor = db.query(
            AlarmContract.AlarmEntry.TABLE_NAME,
            null,
            "${AlarmContract.AlarmEntry.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val hour = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_HOUR))
            val minute = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_MINUTE))
            val daysString = cursor.getString(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_DAYS))
            val label = cursor.getString(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_LABEL))
            val sound = cursor.getString(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_SOUND))
            val enabled = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_ENABLED)) == 1
            val challengeType = cursor.getString(cursor.getColumnIndexOrThrow(AlarmContract.AlarmEntry.COLUMN_CHALLENGE_TYPE))


            val days = Alarm.daysFromString(daysString)

            Alarm(id, hour, minute, days, label, sound, enabled, challengeType)
        } else {
            null
        }.also { cursor.close() }
    }






}