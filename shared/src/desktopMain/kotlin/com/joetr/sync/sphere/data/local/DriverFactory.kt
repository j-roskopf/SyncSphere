package com.joetr.sync.sphere.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.joetr.sync.sphere.SyncSphereRoomDatabase
import com.joetr.sync.sphere.data.RoomConstants

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${RoomConstants.DATABASE_NAME}")

        SyncSphereRoomDatabase.Schema.create(driver)

        return driver
    }
}
