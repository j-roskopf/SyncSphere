package com.joetr.sync.sphere.ui.time

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TimeSelectionButtons(
    days: List<DayTimeItem>,
    submitAvailability: () -> Unit,
    noPreferenceOnTime: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            exit = slideOutHorizontally(
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            ) {
                it * 2
            },
            visible = days.any {
                it.dayTime !is DayTime.NotSelected
            }.not(),
        ) {
            Button(
                onClick = {
                    noPreferenceOnTime()
                },
                modifier = Modifier
                    .padding(16.dp)
                    .defaultMinSize(minHeight = 48.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "No Preference",
                )
            }
        }

        AnimatedVisibility(
            enter = slideInHorizontally(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            ) {
                it * -1
            },
            visible = days.all {
                it.dayTime !is DayTime.NotSelected
            },
        ) {
            Button(
                onClick = {
                    submitAvailability()
                },
                modifier = Modifier
                    .padding(16.dp)
                    .defaultMinSize(minHeight = 48.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    "Submit Availability",
                    maxLines = 1,
                )
            }
        }
    }
}
