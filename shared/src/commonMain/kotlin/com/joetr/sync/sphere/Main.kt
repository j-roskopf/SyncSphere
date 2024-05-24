package com.joetr.sync.sphere

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import com.joetr.sync.sphere.ui.pre.PreRoomScreen

@Composable
fun Main() {
    Navigator(
        screen = PreRoomScreen(),
        disposeBehavior = NavigatorDisposeBehavior(),
        onBackPressed = { true },
    ) { navigator ->
        PlatformNavigatorContent(navigator)
    }
}

@Composable
expect fun PlatformNavigatorContent(navigator: Navigator)
