package com.joetr.sync.sphere.ui.new

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.data.model.JoinedRoom
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.time.TimeSelectionScreen
import com.joetr.sync.sphere.util.format
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class NewRoomScreen(val joinedRoom: JoinedRoom?, val name: String) : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<NewRoomScreenModel>()
        val viewState = screenModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow

        LifecycleEffect(
            onStarted = {
                screenModel.init(
                    joinedRoom = joinedRoom,
                    name = name,
                )
            },
        )

        when (viewState) {
            is NewRoomState.Content -> ContentState(
                roomCode = viewState.roomCode,
                numberOfPeople = viewState.numberOfPeople,
                navigateToTimeSelectionScreen = {
                    navigator.push(
                        TimeSelectionScreen(
                            times = it,
                            roomCode = screenModel.room?.roomCode ?: throw IllegalArgumentException(
                                "Unknown room code",
                            ),
                            personId = screenModel.personId,
                        ),
                    )
                },
                addDate = {
                    screenModel.addDate(it)
                },
                selectedDates = viewState.dates,
                names = viewState.names,
            )

            is NewRoomState.Loading -> LoadingState()
        }
    }

    @Composable
    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalMaterialApi::class,
        ExperimentalLayoutApi::class,
    )
    private fun ContentState(
        roomCode: String,
        numberOfPeople: Int,
        names: List<String>,
        navigateToTimeSelectionScreen: (List<String>) -> Unit,
        addDate: (String) -> Unit,
        selectedDates: List<String>,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Room code: $roomCode",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                Text(
                    text = "# of people here right now: $numberOfPeople",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                Text(
                    text = "Names: ${names.joinToString(", ").trim()}",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                Text(
                    text = "You: $name",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            val state = rememberAdaptiveDatePickerState()

            LaunchedEffect(state.selectedDateMillis) {
                // Do something with the selected date
            }

            AdaptiveDatePicker(
                state = state,
                headline = null,
                title = {
                    // for some reason, if it's null, there is a default provided
                },
            )

            Row {
                Button(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        val localMillis = state.selectedDateMillis
                        if (localMillis != null) {
                            val date = Instant.fromEpochMilliseconds(localMillis)
                                .toLocalDateTime(TimeZone.UTC).format("MM-dd-yyyy")
                            addDate(date)
                        } else {
                            // todo joer - what goes here
                        }
                    },
                ) {
                    Text("Add Date")
                }

                AnimatedVisibility(
                    visible = selectedDates.isNotEmpty(),
                ) {
                    Button(
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            // go to time selection screen
                            navigateToTimeSelectionScreen(selectedDates)
                        },
                    ) {
                        Text("Select Times")
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedDates.isNotEmpty(),
            ) {
                FlowRow {
                    selectedDates.forEach {
                        Chip(
                            onClick = {
                                //
                            },
                            content = {
                                Text(it)
                            },
                            modifier = Modifier.padding(4.dp),
                        )
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
