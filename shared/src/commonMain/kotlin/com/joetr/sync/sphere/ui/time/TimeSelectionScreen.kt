package com.joetr.sync.sphere.ui.time

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.design.button.PrimaryButton
import com.joetr.sync.sphere.design.toolbar.DefaultToolbar
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.pre.collectAsEffect
import com.joetr.sync.sphere.ui.results.ResultsScreen
import com.joetr.sync.sphere.util.formatTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.animation.AnimatedVisibility as ColumnAnimatedVisibility

class TimeSelectionScreen(
    val times: List<String>,
    val roomCode: String,
    private val personId: String,
) : Screen {

    @Composable
    @Suppress("LongMethod")
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
                            previousUserId = null,
                            previousUserName = null,
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

        Scaffold(
            topBar = {
                DefaultToolbar(
                    onBack = {
                        if (viewState is TimeSelectionState.TimeSelection) {
                            screenModel.goBackToContentState()
                        } else {
                            // every other state pops back to the other screen
                            navigator.pop()
                        }
                    },
                )
            },
        ) { paddingValues ->
            AnimatedContent(
                targetState = viewState,
                contentKey = {
                    it.key
                },
            ) { targetState ->
                when (targetState) {
                    is TimeSelectionState.Content -> ContentState(
                        modifier = Modifier.padding(paddingValues),
                        days = targetState.data,
                        allDayClicked = {
                            screenModel.allDayClickedForItem(
                                index = it,
                            )
                        },
                        timeRangeClicked = {
                            screenModel.timeRangeClickedForItem(it)
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

                    is TimeSelectionState.Loading -> LoadingState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is TimeSelectionState.TimeSelection -> TimeSelectionState(
                        modifier = Modifier.padding(paddingValues),
                        index = targetState.index,
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
        }
    }

    @Composable
    private fun ErrorDialog(
        onDismiss: () -> Unit,
        tryAgain: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            confirmButton = {
                PrimaryButton(
                    onClick = {
                        onDismiss()
                    },
                ) {
                    Text("Okay")
                }
            },
            dismissButton = {
                PrimaryButton(
                    onClick = {
                        tryAgain()
                    },
                ) {
                    Text("Try Again")
                }
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @Suppress("LongMethod")
    private fun TimeSelectionState(
        modifier: Modifier = Modifier,
        index: Int,
        timeSelectedForIndex: (Int, DayTime.Range) -> Unit,
        validateStartTime: (DayTime.Range) -> Unit,
        continueButtonEnabled: Boolean,
    ) {
        val startTimeVisible = remember { mutableStateOf(true) }
        val endTimeVisible = remember { mutableStateOf(true) }

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val startTimeState = rememberTimePickerState(
            initialHour = now.hour,
        )
        val endTimeState = rememberTimePickerState(
            initialHour = now.hour,
        )

        val startTimeHour = remember { mutableStateOf(startTimeState.hour) }
        val startTimeMinute = remember { mutableStateOf(startTimeState.minute) }
        val endTimeHour = remember { mutableStateOf(endTimeState.hour) }
        val endTimeMinute = remember { mutableStateOf(endTimeState.minute) }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxSize().verticalScroll(rememberScrollState()),
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

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp)
                        .defaultMinSize(minHeight = 64.dp)
                        .clickable {
                            startTimeVisible.value = startTimeVisible.value.not()
                            endTimeVisible.value = false
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val formattedStartTime = formatTime(startTimeHour.value, startTimeMinute.value)

                    Text(
                        text = "Start Time $formattedStartTime",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                    )
                }

                ColumnAnimatedVisibility(
                    visible = startTimeVisible.value,
                ) {
                    TimePicker(
                        state = startTimeState,
                    )
                }

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

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp)
                        .defaultMinSize(minHeight = 64.dp)
                        .clickable {
                            endTimeVisible.value = endTimeVisible.value.not()
                            startTimeVisible.value = false
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val formattedEndTime = formatTime(endTimeHour.value, endTimeMinute.value)

                    Text(
                        text = "End Time $formattedEndTime",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                    )
                }

                ColumnAnimatedVisibility(
                    visible = endTimeVisible.value,
                ) {
                    TimePicker(
                        state = endTimeState,
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ColumnAnimatedVisibility(
                visible = !continueButtonEnabled,
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Please select an end time after the start time",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            PrimaryButton(
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
        modifier: Modifier = Modifier,
        days: List<DayTimeItem>,
        allDayClicked: (Int) -> Unit,
        timeRangeClicked: (Int) -> Unit,
        submitAvailability: () -> Unit,
        noPreferenceOnTime: () -> Unit,
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
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
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                TimeSelectionButtons(
                    days = days,
                    submitAvailability = submitAvailability,
                    noPreferenceOnTime = noPreferenceOnTime,
                )
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
                    PrimaryButton(
                        modifier = Modifier.padding(8.dp),
                        colors = if (dayTimeItem.dayTime is DayTime.AllDay) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                        onClick = {
                            allDayClicked()
                        },
                    ) {
                        Text("All Day")
                    }
                    PrimaryButton(
                        modifier = Modifier.padding(8.dp),
                        colors = if (dayTimeItem.dayTime is DayTime.Range) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        },
                        onClick = {
                            timeRangeClicked()
                        },
                    ) {
                        Text("Time Range")
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
