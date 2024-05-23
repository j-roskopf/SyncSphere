package com.joetr.sync.sphere.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import syncsphere.shared.generated.resources.Res

private val cache: MutableMap<String, Font> = mutableMapOf()

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun font(name: String, res: String, weight: FontWeight, style: FontStyle): Font {
    return cache.getOrPut(res) {
        val byteArray = runBlocking {
            Res.readBytes("font/$res.ttf")
        }
        androidx.compose.ui.text.platform.Font(name, byteArray, weight, style)
    }
}
