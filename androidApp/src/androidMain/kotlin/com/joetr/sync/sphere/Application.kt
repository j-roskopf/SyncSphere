package com.joetr.sync.sphere

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.joetr.sync.sphere.data.BuildConfig
import com.joetr.sync.sphere.data.BuildConfigImpl
import initCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class Application : Application() {
    private val buildConfigModule = module {
        single<BuildConfig> { BuildConfigImpl(get()) }
    }

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)

        initKoin(
            block = {
                androidContext(this@Application)
            },
            modules = listOf(buildConfigModule),
        )
        initCrashlytics()
    }
}
