package com.joetr.sync.sphere.data

import android.content.Context
import android.content.pm.ApplicationInfo

actual class BuildConfigImpl(private val applicationContext: Context) : BuildConfig {
    override fun isDebug(): Boolean {
        return 0 != applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    }
}
