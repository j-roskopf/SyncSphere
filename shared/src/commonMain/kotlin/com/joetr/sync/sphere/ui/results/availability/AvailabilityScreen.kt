package com.joetr.sync.sphere.ui.results.availability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.joetr.sync.sphere.ui.results.data.ALL_DAY
import com.joetr.sync.sphere.ui.results.data.NONE
import com.joetr.sync.sphere.ui.results.data.TimeRange

class AvailabilityScreen(
    val data: Map<String, TimeRange>,
) : Screen {

    @Composable
    override fun Content() {
        AvailabilityState(
            data = data,
        )
    }

    @Composable
    private fun AvailabilityState(data: Map<String, TimeRange>) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn {
                data.onEachIndexed { index, entry ->
                    item {
                        AvailabilityDayItem(entry)

                        if (index != data.size - 1) {
                            Divider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AvailabilityDayItem(entry: Map.Entry<String, TimeRange>) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = entry.key,
            )

            val text = when (val range = entry.value) {
                NONE -> {
                    "No availability on day"
                }
                ALL_DAY -> {
                    "This day works for everyone all day"
                }
                else -> {
                    "This day works for everyone between $range"
                }
            }

            Text(
                modifier = Modifier.weight(3f),
                text = text,
            )
        }
    }
}
