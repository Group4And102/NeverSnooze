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
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Alarms.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create the alarms table
        db.execSQL(AlarmContract.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply discard the data and start over
        db.execSQL(AlarmContract.SQL_DELETE_TABLE)
        onCreate(db)
    }
}