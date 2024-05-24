@file:Suppress("MagicNumber")

package com.joetr.sync.sphere

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.stack.StackEvent
import kotlin.math.PI
import kotlin.math.cos

abstract class NavigatorScreenTransition {
    var lastStackEvent by mutableStateOf(StackEvent.Idle)
    var isAnimatingIn by mutableStateOf(false)
    var isAnimatingAway by mutableStateOf(false)
    var transitionAnimatable = Animatable(0f)
    var easeFunc: (Float) -> Float = { (-0.5f * (cos(PI * it) - 1f)).toFloat() }

    fun startPeeking(isPrevScreen: Boolean) {
        this.lastStackEvent = StackEvent.Pop
        this.isAnimatingIn = isPrevScreen
        this.isAnimatingAway = !isPrevScreen
    }

    suspend fun stopPeeking() {
        val durationMillis = 250f * (1f - transitionAnimatable.value)

        transitionAnimatable.animateTo(0f, tween(durationMillis.toInt(), easing = LinearEasing))
    }

    suspend fun startTransition(
        lastStackEvent: StackEvent,
        isAnimatingIn: Boolean = false,
        isAnimatingAway: Boolean = false,
    ) {
        this.lastStackEvent = lastStackEvent
        this.isAnimatingIn = isAnimatingIn
        this.isAnimatingAway = isAnimatingAway

        transitionAnimatable.animateTo(1f, tween(250, easing = LinearEasing))
    }

    abstract fun Modifier.animatingModifier(): Modifier
}

class SlideTransition : NavigatorScreenTransition() {
    override fun Modifier.animatingModifier(): Modifier =
        composed {
            var modifier = this

            val isPop = lastStackEvent == StackEvent.Pop

            val transitionFractionState by remember(transitionAnimatable.value) {
                transitionAnimatable.asState()
            }

            val transitionFraction by remember(transitionFractionState) {
                derivedStateOf { easeFunc(transitionFractionState) }
            }

            if (isAnimatingAway) {
                modifier = if (isPop) {
                    modifier.slideFraction(transitionFraction)
                } else {
                    modifier
                        .background(MaterialTheme.colorScheme.background)
                        .slideFraction(-0.25f * transitionFraction)
                        .drawWithContent {
                            drawContent()
                            drawRect(Color.Black, alpha = transitionFraction * 0.25f)
                        }
                }
            } else if (isAnimatingIn) {
                modifier = if (isPop) {
                    modifier
                        .background(MaterialTheme.colorScheme.background)
                        .slideFraction(-0.25f + (0.25f * transitionFraction))
                        .drawWithContent {
                            drawContent()
                            drawRect(Color.Black, alpha = 0.25f - (transitionFraction * 0.25f))
                        }
                } else {
                    modifier.slideFraction(1f - transitionFraction)
                }
            }

            modifier
        }

    private fun Modifier.slideFraction(fraction: Float): Modifier =
        this.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val measuredSize = IntSize(placeable.width, placeable.height)

            layout(placeable.width, placeable.height) {
                val slideValue = (measuredSize.width.toFloat() * fraction).toInt()

                placeable.placeWithLayer(IntOffset(x = slideValue, y = 0))
            }
        }
}
