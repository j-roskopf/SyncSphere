package com.joetr.sync.sphere

import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FixedThreshold
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.joetr.sync.sphere.design.swipe.ScreenSwipeToDismiss
import com.joetr.sync.sphere.ui.pre.PreRoomScreen
import com.joetr.sync.sphere.util.iOS
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Main() {
    val scope = rememberCoroutineScope()

    Navigator(PreRoomScreen()) { navigator ->
        SlideTransition(
            navigator = navigator,
            content = { screen ->
                val dismissState = key(screen.key) {
                    rememberDismissState {
                        when (it) {
                            DismissValue.DismissedToEnd -> {
                                scope.launch {
                                    navigator.pop()
                                }
                                true
                            }
                            DismissValue.Default -> true
                            DismissValue.DismissedToStart -> {
                                true
                            }
                        }
                    }
                }
                ScreenSwipeToDismiss(
                    state = dismissState,
                    dismissContent = {
                        screen.Content()
                    },
                    dismissThreshold = FixedThreshold(56.dp),
                    // only enabled on iOS
                    enabled = navigator.canPop && iOS,
                    spaceToSwipe = Int.MAX_VALUE.dp,
                )
            },
        )
    }
}
