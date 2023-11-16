package com.joetr.sync.sphere.ui.results.availability

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.design.toolbar.DefaultToolbar
import com.joetr.sync.sphere.design.toolbar.backOrNull
import com.joetr.sync.sphere.ui.time.DayTime
import com.joetr.sync.sphere.ui.time.getDisplayText
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class AvailabilityScreen(
    val data: Map<String, DayTime>,
) : Screen {

    @Composable
    override fun Content() {
        Scaffold(
            topBar = {
                DefaultToolbar(
                    onBack = LocalNavigator.currentOrThrow.backOrNull(),
                )
            },
        ) { paddingValues ->
            AvailabilityState(
                modifier = Modifier.padding(paddingValues),
                data = data,
            )
        }
    }

    @Composable
    private fun AvailabilityState(
        modifier: Modifier = Modifier,
        data: Map<String, DayTime>,
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
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

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    @Suppress("MagicNumber")
    private fun AvailabilityDayItem(entry: Map.Entry<String, DayTime>) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val image = when (entry.value) {
                is DayTime.AllDay -> "full_availability.png"
                is DayTime.NotSelected -> "no_availability.png"
                is DayTime.Range -> "partial_availability.png"
            }

            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                modifier = Modifier.weight(1.5f),
                text = entry.key,
            )

            val text = when (val range = entry.value) {
                is DayTime.AllDay -> "This day works for everyone all day"
                is DayTime.NotSelected -> "No availability on day"
                is DayTime.Range -> "This day works for everyone between ${range.getDisplayText()}"
            }

            Text(
                modifier = Modifier.weight(3f),
                text = text,
            )
        }
    }
}
