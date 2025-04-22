package com.example.neversnooze

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_table")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "time") val time: String,
    @ColumnInfo(name = "label") val label: String?,
    @ColumnInfo(name = "repeatDays") val repeatDays: String?,
    @ColumnInfo(name = "isEnabled") val isEnabled: Boolean = true
)
