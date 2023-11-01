package com.joetr.sync.sphere.util

import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString()
