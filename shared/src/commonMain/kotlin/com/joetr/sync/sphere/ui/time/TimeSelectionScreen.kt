package com.joetr.sync.sphere.ui.time

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.pre.collectAsEffect
import com.joetr.sync.sphere.ui.results.ResultsScreen
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog

class TimeSelectionScreen(
    val times: List<String>,
    val roomCode: String,
    private val personId: String,
) : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<TimeSelectionScreenModel>()
        val viewState = screenModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow
        var continueButtonEnabled = remember { mutableStateOf(false) }
        var displayErrorDialog = remember { mutableStateOf(false) }

        LifecycleEffect(
            onStarted = {
                screenModel.initializeData(times, roomCode)
            },
        )

        screenModel.action.collectAsEffect {
            when (it) {
                is TimeSelectionScreenActions.NavigateToResults -> {
                    navigator.push(
                        ResultsScreen(
                            roomCode = roomCode,
                        ),
                    )
                }

                TimeSelectionScreenActions.ErrorOccurred -> {
                    displayErrorDialog.value = true
                }
            }
        }

        if (displayErrorDialog.value) {
            ErrorDialog(
                onDismiss = {
                    displayErrorDialog.value = false
                },
                tryAgain = {
                    displayErrorDialog.value = false

                    screenModel.submitAvailability(
                        roomCode = roomCode,
                        personId = personId,
                    )
                },
            )
        }

        when (viewState) {
            is TimeSelectionState.Content -> ContentState(
                days = viewState.data,
                allDayClicked = {
                    screenModel.allDayClickedForItem(
                        index = it,
                    )
                },
                timeRangeClicked = {
                    screenModel.switchToTimePicking(it)
                },
                submitAvailability = {
                    screenModel.submitAvailability(
                        roomCode = roomCode,
                        personId = personId,
                    )
                },
                noPreferenceOnTime = {
                    screenModel.noPreference()
                },
            )

            is TimeSelectionState.Loading -> LoadingState()
            is TimeSelectionState.TimeSelection -> TimeSelectionState(
                index = viewState.index,
                timeSelectedForIndex = { index, range ->
                    screenModel.rangeClickedForItem(index, range)
                },
                validateStartTime = {
                    continueButtonEnabled.value = if (it.endTimeHour > it.startTimeHour) {
                        true
                    } else if (it.startTimeHour == it.endTimeHour) {
                        it.endTimeMinute > it.startTimeMinute
                    } else {
                        false
                    }
                },
                continueButtonEnabled = continueButtonEnabled.value,
            )
        }
    }

    @Composable
    private fun ErrorDialog(
        onDismiss: () -> Unit,
        tryAgain: () -> Unit,
    ) {
        AdaptiveAlertDialog(
            title = "Error",
            text = "An error occurred",
            confirmText = "Okay",
            dismissText = "Try Again",
            onConfirm = {
                onDismiss()
            },
            onDismiss = {
                tryAgain()
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TimeSelectionState(
        index: Int,
        timeSelectedForIndex: (Int, DayTime.Range) -> Unit,
        validateStartTime: (DayTime.Range) -> Unit,
        continueButtonEnabled: Boolean,
    ) {
        val startTimeState = rememberTimePickerState()
        val endTimeState = rememberTimePickerState()

        val startTimeHour = remember { mutableStateOf(startTimeState.hour) }
        val startTimeMinute = remember { mutableStateOf(startTimeState.minute) }
        val endTimeHour = remember { mutableStateOf(endTimeState.hour) }
        val endTimeMinute = remember { mutableStateOf(endTimeState.minute) }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LaunchedEffect(startTimeState.hour, startTimeState.minute) {
                    startTimeMinute.value = startTimeState.minute
                    startTimeHour.value = startTimeState.hour

                    validateStartTime(
                        DayTime.Range(
                            startTimeHour = startTimeHour.value,
                            endTimeHour = endTimeHour.value,
                            startTimeMinute = startTimeMinute.value,
                            endTimeMinute = endTimeMinute.value,
                        ),
                    )
                }

                Text(
                    text = "Start Time",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(8.dp),
                )

                TimePicker(
                    state = startTimeState,
                )

                LaunchedEffect(endTimeState.hour, endTimeState.minute) {
                    endTimeMinute.value = endTimeState.minute
                    endTimeHour.value = endTimeState.hour

                    validateStartTime(
                        DayTime.Range(
                            startTimeHour = startTimeHour.value,
                            endTimeHour = endTimeHour.value,
                            startTimeMinute = startTimeMinute.value,
                            endTimeMinute = endTimeMinute.value,
                        ),
                    )
                }

                Text(
                    text = "End Time",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(8.dp),
                )

                TimePicker(
                    state = endTimeState,
                )
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            Button(
                enabled = continueButtonEnabled,
                onClick = {
                    timeSelectedForIndex(
                        index,
                        DayTime.Range(
                            startTimeHour = startTimeHour.value,
                            endTimeHour = endTimeHour.value,
                            startTimeMinute = startTimeMinute.value,
                            endTimeMinute = endTimeMinute.value,
                        ),
                    )
                },
                modifier = Modifier.defaultMinSize(minHeight = 64.dp).fillMaxWidth().padding(8.dp),
            ) {
                Text("Select Times")
            }
        }
    }

    @Composable
    private fun ContentState(
        days: List<DayTimeItem>,
        allDayClicked: (Int) -> Unit,
        timeRangeClicked: (Int) -> Unit,
        submitAvailability: () -> Unit,
        noPreferenceOnTime: () -> Unit,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                days.forEachIndexed { index, dayTimeItem ->
                    item {
                        DayTimeItem(
                            dayTimeItem = dayTimeItem,
                            allDayClicked = {
                                allDayClicked(index)
                            },
                            timeRangeClicked = {
                                timeRangeClicked(index)
                            },
                        )
                    }
                }
            }

            Column {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    AnimatedVisibility(
                        modifier = Modifier.padding(8.dp)
                            .weight(1f),
                        visible = days.all {
                            it.dayTime !is DayTime.NotSelected
                        },
                    ) {
                        Button(
                            onClick = {
                                submitAvailability()
                            },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp),
                        ) {
                            Text("Submit Availability")
                        }
                    }

                    Button(
                        onClick = {
                            noPreferenceOnTime()
                        },
                        modifier = Modifier.padding(8.dp)
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        Text("No Preference")
                    }
                }
            }
        }
    }

    @Composable
    @Suppress("MagicNumber")
    private fun DayTimeItem(
        dayTimeItem: DayTimeItem,
        allDayClicked: () -> Unit,
        timeRangeClicked: () -> Unit,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        dayTimeItem.dayTime !is DayTime.NotSelected,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            "check",
                        )
                    }

                    Text(dayTimeItem.display)
                }
                Column(
                    modifier = Modifier.weight(1.5f).padding(horizontal = 16.dp),
                ) {
                    Text(dayTimeItem.dayTime.getDisplayText())
                    Button(
                        onClick = {
                            allDayClicked()
                        },
                    ) {
                        Text("All Day")
                    }
                    Button(
                        onClick = {
                            timeRangeClicked()
                        },
                    ) {
                        Text("Select Time Range")
                    }
                }
            }
        }
    }

    @Composable
    private fun LoadingState() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ProgressIndicator()
        }
    }
}
