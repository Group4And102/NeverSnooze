package com.example.neversnooze.alarmscheduler

import java.time.LocalDateTime

// holds the data for each of our alarms
// TODO add a resource link here to play unique music per alarm
// TODO repeating alarm on certain days
// TODO mission type
data class AlarmItem(
    val label: String,
    val time: LocalDateTime

)
