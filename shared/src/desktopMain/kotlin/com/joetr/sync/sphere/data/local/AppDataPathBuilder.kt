package com.joetr.sync.sphere.data.local

import java.io.File

internal object AppDataPathBuilder {

    private val appDataPath = when {
        System.getProperty("os.name").contains("Mac", true) -> {
            "${System.getProperty("user.home")}/Library/Application Support/SyncSphere"
        }
        System.getProperty("os.name").contains("windows", true) -> {
            "${System.getProperty("user.home")}\\AppData\\Local\\SyncSphere"
        }
        else -> {
            error("This type OS not implemented")
        }
    }

    fun getAppDataPath(): String {
        val appPath = appDataPath
        if (!File(appPath).exists()) {
            File(appPath).mkdirs()
        }
        return appPath
    }
}
