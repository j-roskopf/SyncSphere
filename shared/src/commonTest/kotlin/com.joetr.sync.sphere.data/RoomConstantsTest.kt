package com.joetr.sync.sphere.data

import kotlin.test.Test
import kotlin.test.assertEquals

class RoomConstantsTest {

    @Test
    fun `debug environment gives debug room collection`() {
        val debugBuildConfig = object : BuildConfig {
            override fun isDebug() = true
        }
        val roomConstants = RoomConstants(
            buildConfig = debugBuildConfig,
        )
        assertEquals("RoomsTesting", roomConstants.roomCollection())
    }

    @Test
    fun `prod environment gives prod room collection`() {
        val debugBuildConfig = object : BuildConfig {
            override fun isDebug() = false
        }
        val roomConstants = RoomConstants(
            buildConfig = debugBuildConfig,
        )
        assertEquals("RoomsAuthenticated", roomConstants.roomCollection())
    }
}
