package com.joetr.sync.sphere

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import initCrashlytics

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        initKoin()
        initCrashlytics()
    }
}
