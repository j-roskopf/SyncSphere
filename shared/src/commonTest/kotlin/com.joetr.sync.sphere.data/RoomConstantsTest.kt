package com.joetr.sync.sphere.data

import kotlin.test.Test
import kotlin.test.assertEquals

class RoomConstantsTest {

    @Test
    fun `make sure I do not change the collections accidentally`() {
        assertEquals("RoomsAuthenticated", RoomConstants.ROOM_COLLECTION)
    }
}
