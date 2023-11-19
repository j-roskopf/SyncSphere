package com.joetr.sync.sphere.data

import com.joetr.sync.sphere.constants.Dictionary

expect class RoomRepositoryImpl(
    dictionary: Dictionary,
    crashReporting: CrashReporting,
    roomConstants: RoomConstants,
) : RoomRepository
