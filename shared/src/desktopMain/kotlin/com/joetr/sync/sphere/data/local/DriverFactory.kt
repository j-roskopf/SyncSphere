package com.joetr.sync.sphere.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.joetr.sync.sphere.SyncSphereRoomDatabase
import com.joetr.sync.sphere.data.RoomConstants
import java.io.File
import java.util.Properties

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val appPath = AppDataPathBuilder.getAppDataPath()

        val databasePath = File(appPath, "/${RoomConstants.DATABASE_NAME}")

        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY + databasePath.absolutePath, Properties())

        SyncSphereRoomDatabase.Schema.create(driver)

        return driver
    }
}
