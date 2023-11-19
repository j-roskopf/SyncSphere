package com.joetr.sync.sphere.data

actual class BuildConfigImpl() : BuildConfig {

    override fun isDebug(): Boolean {
        val debugSystemProperty = System.getProperty("syncSphereDebug")
        return debugSystemProperty?.toBoolean() ?: false
    }
}
