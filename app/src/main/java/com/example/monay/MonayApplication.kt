package com.example.monay

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MonayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}