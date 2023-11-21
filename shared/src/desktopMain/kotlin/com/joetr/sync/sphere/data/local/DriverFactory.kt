package com.joetr.sync.sphere.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.joetr.sync.sphere.data.RoomConstants

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return JdbcSqliteDriver("jdbc:sqlite:${RoomConstants.DATABASE_NAME}")
    }
}
