package com.joetr.sync.sphere

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.joetr.sync.sphere.ui.pre.PreRoomScreen

@Composable fun Main() {
    Navigator(PreRoomScreen()) { navigator ->
        SlideTransition(navigator)
    }
}
