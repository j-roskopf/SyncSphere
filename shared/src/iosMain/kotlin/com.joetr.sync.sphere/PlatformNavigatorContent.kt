package com.joetr.sync.sphere

import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import kotlinx.coroutines.launch

enum class DismissValue {
    Default,
    DismissedToEnd,
}

@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
actual fun PlatformNavigatorContent(navigator: Navigator) {
    val density = LocalDensity.current

    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<ScreenHolder?>(null) }
    val animatedScreens = remember { mutableStateListOf<ScreenHolder>() }
    var peekingScreen by remember { mutableStateOf<ScreenHolder?>(null) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxWidthPx = constraints.maxWidth.toFloat()

        val anchors by remember(maxWidthPx) {
            derivedStateOf {
                DraggableAnchors {
                    DismissValue.Default at 0f
                    DismissValue.DismissedToEnd at maxWidthPx
                }
            }
        }

        val anchoredDraggableState by remember {
            derivedStateOf {
                AnchoredDraggableState(
                    initialValue = DismissValue.Default,
                    anchors = anchors,
                    positionalThreshold = { distance -> distance * 0.4f },
                    velocityThreshold = { with(density) { 125.dp.toPx() } },
                    animationSpec = SpringSpec(),
                )
            }
        }

        LaunchedEffect(anchors) {
            anchoredDraggableState.updateAnchors(anchors)
        }

        val lastEvent = navigator.lastEvent

        val currentValue = anchoredDraggableState.currentValue
        val offset = anchoredDraggableState.offset

        LaunchedEffect(navigator.lastItemOrNull?.key) {
            if (navigator.lastItemOrNull != null) {
                // Remove all transitions when lastItem is changed
                animatedScreens.forEach { it.transition = null }

                val foundScreen = animatedScreens.findLast { it.screen == navigator.lastItem }
                val newScreen = foundScreen ?: ScreenHolder(navigator.lastItem)

                // Screen can already be in animatedScreens when peeking
                if (foundScreen == null) {
                    if (lastEvent == StackEvent.Pop) {
                        animatedScreens.add(0, newScreen)
                    } else {
                        animatedScreens.add(newScreen)
                    }
                }

                currentScreen?.let { currentScreen ->
                    if (currentValue == DismissValue.Default) {
                        if (currentScreen.transition == null) {
                            currentScreen.transition = SlideTransition()
                        }

                        if (newScreen.transition == null) {
                            newScreen.transition = SlideTransition()
                        }

                        coroutineScope.launch {
                            newScreen.transition?.startTransition(
                                lastStackEvent = lastEvent,
                                isAnimatingIn = true,
                            )

                            newScreen.transition = null
                        }

                        coroutineScope.launch {
                            currentScreen.transition?.startTransition(
                                lastStackEvent = lastEvent,
                                isAnimatingAway = true,
                            )

                            animatedScreens.remove(currentScreen)
                        }
                    } else {
                        animatedScreens.remove(currentScreen)
                    }
                }

                currentScreen = newScreen
                anchoredDraggableState.anchoredDrag { dragTo(0f) }
            }
        }

        LaunchedEffect(offset) {
            if (currentValue == DismissValue.Default) {
                if (offset > 0f) {
                    if (currentScreen?.transition == null && navigator.size >= 2) {
                        currentScreen?.transition = SlideTransition()
                        currentScreen?.transition?.startPeeking(isPrevScreen = false)

                        peekingScreen = ScreenHolder(navigator.items[navigator.size - 2])

                        peekingScreen?.let { peekingScreen ->
                            peekingScreen.transition = SlideTransition()
                            peekingScreen.transition?.startPeeking(isPrevScreen = true)

                            animatedScreens.add(0, peekingScreen)
                        }
                    }

                    peekingScreen?.let { peekingScreen ->
                        val peekingFraction = offset / maxWidthPx

                        coroutineScope.launch {
                            currentScreen?.transition?.transitionAnimatable?.snapTo(peekingFraction)
                        }

                        coroutineScope.launch {
                            peekingScreen.transition?.transitionAnimatable?.snapTo(peekingFraction)
                        }
                    }
                } else {
                    peekingScreen?.let { peekingScreen ->
                        currentScreen?.let { currentScreen ->
                            coroutineScope.launch {
                                currentScreen.transition?.stopPeeking()
                                currentScreen.transition = null
                            }
                        }

                        coroutineScope.launch {
                            peekingScreen.transition?.stopPeeking()
                            peekingScreen.transition = null
                            animatedScreens.remove(peekingScreen)
                        }
                    }

                    peekingScreen = null
                }
            }
        }

        LaunchedEffect(currentValue) {
            if (currentValue == DismissValue.DismissedToEnd) {
                peekingScreen = null

                if (navigator.canPop) {
                    navigator.pop()
                } else {
                    navigator.parent?.pop()
                }
            }
        }

        val currentScreenModifier = Modifier.anchoredDraggable(
            state = anchoredDraggableState,
            orientation = Orientation.Horizontal,
            enabled = navigator.canPop && currentValue == DismissValue.Default,
            reverseDirection = LocalLayoutDirection.current == LayoutDirection.Rtl,
        )

        animatedScreens.fastForEach { screen ->
            key(screen.screen.key) {
                navigator.saveableState("transition", screen.screen) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .then(if (screen == currentScreen) currentScreenModifier else Modifier)
                            .animatingModifier(screen),
                    ) {
                        screen.screen.Content()
                    }
                }
            }
        }
    }
}

private fun Modifier.animatingModifier(screenHolder: ScreenHolder) =
    screenHolder.run { this@animatingModifier.animatingModifier() }

private class ScreenHolder(val screen: Screen) {
    var transition by mutableStateOf<NavigatorScreenTransition?>(null)

    fun Modifier.animatingModifier(): Modifier = transition?.run { this@animatingModifier.animatingModifier() } ?: this
}
