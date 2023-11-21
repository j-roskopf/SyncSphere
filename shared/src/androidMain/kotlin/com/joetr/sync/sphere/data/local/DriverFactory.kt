package com.joetr.sync.sphere.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.joetr.sync.sphere.SyncSphereRoomDatabase
import com.joetr.sync.sphere.data.RoomConstants.Companion.DATABASE_NAME

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(SyncSphereRoomDatabase.Schema, context, DATABASE_NAME)
    }
}
