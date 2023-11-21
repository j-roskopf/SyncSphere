package com.joetr.sync.sphere.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.joetr.sync.sphere.SyncSphereRoomDatabase
import com.joetr.sync.sphere.data.RoomConstants.Companion.DATABASE_NAME

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(SyncSphereRoomDatabase.Schema, DATABASE_NAME)
    }
}
