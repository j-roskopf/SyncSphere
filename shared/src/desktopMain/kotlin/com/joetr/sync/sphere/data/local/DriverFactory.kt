package com.joetr.sync.sphere.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.joetr.sync.sphere.SyncSphereRoomDatabase
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
        ).also { db ->
            SyncSphereRoomDatabase.Schema.create(db)
        }
    }

    private val databaseFile: File
        get() = File(appDir.also { if (!it.exists()) it.mkdirs() }, "syncsphere.db")

    private val appDir: File
        get() {
            val os = System.getProperty("os.name").lowercase()
            return when {
                os.contains("win") -> {
                    File(System.getenv("AppData"), "syncsphere/db")
                }

                os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
                    File(System.getProperty("user.home"), ".syncsphere")
                }

                os.contains("mac") -> {
                    File(System.getProperty("user.home"), "Library/Application Support/syncsphere")
                }

                else -> error("Unsupported operating system")
            }
        }
}
