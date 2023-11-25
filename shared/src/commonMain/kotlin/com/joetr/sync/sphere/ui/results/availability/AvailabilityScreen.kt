package com.joetr.sync.sphere.ui.results.availability

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.design.button.PrimaryButton
import com.joetr.sync.sphere.design.theme.conditional
import com.joetr.sync.sphere.design.toolbar.DefaultToolbar
import com.joetr.sync.sphere.design.toolbar.backOrNull
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.results.availability.data.DayStatus
import com.joetr.sync.sphere.ui.time.DayTime
import com.joetr.sync.sphere.ui.time.getDisplayText
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class AvailabilityScreen(
    val data: Map<String, DayTime>,
) : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<AvailabilityScreenModel>()
        val viewState = screenModel.state.collectAsState().value

        LifecycleEffect(
            onStarted = {
                screenModel.init(data)
            },
        )

        Scaffold(
            topBar = {
                DefaultToolbar(
                    onBack = LocalNavigator.currentOrThrow.backOrNull(),
                )
            },
        ) { paddingValues ->
            AnimatedContent(
                targetState = viewState,
            ) { targetState ->
                when (targetState) {
                    is AvailabilityScreenState.Content -> AvailabilityState(
                        modifier = Modifier.padding(paddingValues),
                        data = targetState.data,
                    ) { localDate, dayTime ->
                        screenModel.addToCalendar(localDate, dayTime)
                    }

                    is AvailabilityScreenState.Loading -> LoadingState(
                        modifier = Modifier.padding(
                            paddingValues,
                        ),
                    )
                }
            }
        }
    }

    @Composable
    private fun AvailabilityState(
        modifier: Modifier = Modifier,
        data: Map<DayStatus, List<Pair<String, DayTime>>>,
        addToCalendar: (LocalDate, DayTime) -> Unit,
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn {
                val daysDoWorkEntry = data[DayStatus.DAY_WORKS]!!

                daysDoWorkEntry.onEachIndexed { index, entry ->
                    item {
                        AvailabilityDayItem(
                            displayDay = entry.first,
                            dayTime = entry.second,
                            addToCalendar = addToCalendar,
                        )

                        if (index != daysDoWorkEntry.size - 1) {
                            Divider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }

                val daysDoNotWorkEntry = data[DayStatus.DAY_DOES_NOT_WORK]!!
                if (daysDoNotWorkEntry.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = "Days That Do Not Work For Everyone",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }
                daysDoNotWorkEntry.onEachIndexed { index, entry ->
                    item {
                        AvailabilityDayItem(
                            displayDay = entry.first,
                            dayTime = entry.second,
                            addToCalendar = addToCalendar,
                        )

                        if (index != daysDoNotWorkEntry.size - 1) {
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
    private fun AvailabilityDayItem(
        displayDay: String,
        dayTime: DayTime,
        addToCalendar: (LocalDate, DayTime) -> Unit,
    ) {
        val isActionRowVisible = remember { mutableStateOf(false) }
        val shouldDisplayActionRow = dayTime is DayTime.AllDay || dayTime is DayTime.Range
        Column(
            modifier = Modifier.fillMaxWidth()
                .conditional(
                    shouldDisplayActionRow,
                    {
                        Modifier.clickable {
                            isActionRowVisible.value = isActionRowVisible.value.not()
                        }
                    },
                )
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val image = when (dayTime) {
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
                    text = displayDay,
                )

                val text = when (dayTime) {
                    is DayTime.AllDay -> "This day works for everyone all day"
                    is DayTime.NotSelected -> "No availability on day"
                    is DayTime.Range -> "This day works for everyone between ${dayTime.getDisplayText()}"
                }

                Text(
                    modifier = Modifier.weight(3f),
                    text = text,
                )

                if (shouldDisplayActionRow) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (shouldDisplayActionRow) {
                AnimatedVisibility(
                    visible = isActionRowVisible.value,
                ) {
                    PrimaryButton(
                        modifier = Modifier.padding(16.dp),
                        onClick = {
                            val availability = LocalDate.parse(displayDay)
                            addToCalendar(availability, dayTime)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add To Calendar")
                    }
                }
            }
        }
    }

    @Composable
    private fun LoadingState(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ProgressIndicator()
        }
    }
}
