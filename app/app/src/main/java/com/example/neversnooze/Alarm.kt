package com.example.neversnooze

/**
 * Data class representing an alarm
 */

data class Alarm (
    val id: Long = -1,
    val hour: Int,
    val minute: Int,
    val days: List<Boolean>, // 7 days rep = 7 days of the week
    val label: String,
    val sound: String,
    val enabled: Boolean
    ) {
    /**
     * Covert the days list to a string format for storage
     */

    fun daysToString(): String {
        return days.joinToString(","){ if (it) "1" else "0"}
    }

    /**
     * Get a formatted time string
     */

    fun getFormattedTime(): String {
        val hourFormatted = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (hour < 12) "AM" else "PM"
        val minuteFormatted = String.format("%02d", minute) // "08:30"
        return "$hourFormatted: $minuteFormatted $amPm"
    }

    /**
     * Get a formatted days string (e.g., "Mon, Wed, Fri")
     */
    fun getFormattedDays(): String {
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val selectedDays = days.mapIndexedNotNull { index, selected ->
            if (selected) dayNames[index] else null
        }

        return when {
            selectedDays.size == 7 -> "Every day"
            selectedDays.size == 0 -> "Never"
            selectedDays.containsAll(listOf("Mon", "Tue", "Wed", "Thu", "Fri")) &&
                    !selectedDays.containsAll(listOf("Sat", "Sun")) -> "Weekdays"
            selectedDays.containsAll(listOf("Sat", "Sun")) &&
                    selectedDays.size == 2 -> "Weekends"
            else -> selectedDays.joinToString(", ")
        }
    }

    companion object {
        /**
         * Convert a comma-separated string of 0s and 1s to a list of booleans
         */
        fun daysFromString(daysString: String): List<Boolean> {
            return daysString.split(",").map { it == "1" }
        }

    }

}
