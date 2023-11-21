package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.SyncSphereRoomDatabase
import com.joetr.sync.sphere.constants.Dictionary
import com.joetr.sync.sphere.crash.CrashReporting
import com.russhwolf.settings.Settings

expect class RoomRepositoryImpl(
    dictionary: Dictionary,
    crashReporting: CrashReporting,
    roomConstants: RoomConstants,
    syncSphereRoomDatabase: SyncSphereRoomDatabase,
    settings: Settings,
) : RoomRepository
