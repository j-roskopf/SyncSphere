package com.joetr.sync.sphere

import MainView
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            // todo joer move to application
            Firebase.initialize(this)
            initKoin()
            initCrashlytics()
        }

        setContent {
            MainView()
        }
    }
}
