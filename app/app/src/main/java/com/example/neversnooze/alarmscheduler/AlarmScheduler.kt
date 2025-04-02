package com.example.neversnooze.alarmscheduler

interface AlarmScheduler {
    fun schedule(item : AlarmItem)
    fun cancel(item: AlarmItem)
}