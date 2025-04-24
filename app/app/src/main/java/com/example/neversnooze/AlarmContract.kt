package com.example.neversnooze

import android.provider.BaseColumns

/**
 * Contract class for defining the database schema
 */
object AlarmContract {
    // Table contents defined as a nested class
    object AlarmEntry : BaseColumns {
        const val TABLE_NAME = "alarms"
        const val COLUMN_ID = BaseColumns._ID
        const val COLUMN_HOUR = "hour"
        const val COLUMN_MINUTE = "minute"
        const val COLUMN_DAYS = "days"
        const val COLUMN_LABEL = "label"
        const val COLUMN_SOUND = "sound"
        const val COLUMN_ENABLED = "enabled"
    }

    // SQL statement to create the table
    const val SQL_CREATE_TABLE = """
        CREATE TABLE ${AlarmEntry.TABLE_NAME} (
            ${AlarmEntry.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${AlarmEntry.COLUMN_HOUR} INTEGER NOT NULL,
            ${AlarmEntry.COLUMN_MINUTE} INTEGER NOT NULL,
            ${AlarmEntry.COLUMN_DAYS} TEXT NOT NULL,
            ${AlarmEntry.COLUMN_LABEL} TEXT,
            ${AlarmEntry.COLUMN_SOUND} TEXT NOT NULL,
            ${AlarmEntry.COLUMN_ENABLED} INTEGER NOT NULL
        )
    """

    // SQL statement to delete the table
    const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS ${AlarmEntry.TABLE_NAME}"
}