package com.example.neversnooze

import android.app.Application

class NeverSnoozeApplication : Application() {
    val database: NeverSnoozeDatabase by lazy {
        NeverSnoozeDatabase.getInstance(this)
    }
}