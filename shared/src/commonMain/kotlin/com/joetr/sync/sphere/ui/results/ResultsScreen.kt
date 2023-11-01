package com.joetr.sync.sphere.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.data.model.Room
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.pre.collectAsEffect
import com.joetr.sync.sphere.ui.results.availability.AvailabilityScreen
import com.joetr.sync.sphere.ui.time.getDisplayText

class ResultsScreen(
    val roomCode: String,
) : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<ResultsScreenModel>()
        val viewState = screenModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow

        LifecycleEffect(
            onStarted = {
                screenModel.initializeData(
                    roomCode,
                )
            },
        )

        screenModel.action.collectAsEffect {
            when (it) {
                is ResultsScreenAction.NavigateToResults -> {
                    navigator.push(AvailabilityScreen(it.timeRanges))
                }
            }
        }

        when (viewState) {
            is ResultsScreenState.Content -> ContentState(
                room = viewState.room,
                calculateAvailability = {
                    screenModel.calculateAvailability(it)
                },
            )
            is ResultsScreenState.Loading -> LoadingState()
        }
    }

    @Composable
    private fun ContentState(
        room: Room,
        calculateAvailability: (List<People>) -> Unit,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Text(
                        text = "Room code: $roomCode",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Divider(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        ),
                    )
                    Text(
                        text = "# of people here right now: ${room.numberOfPeople}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Divider(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        ),
                    )
                    Text(
                        text = "Names: ${
                            room.people.joinToString(", ") {
                                it.name
                            }.trim()}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Divider(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        ),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            calculateAvailability(room.people)
                        },
                    ) {
                        Text("Calculate availability")
                    }
                }
                room.people.forEachIndexed { index, person ->
                    item {
                        PersonWithAvailability(person)
                        if (index != room.people.size - 1) {
                            Divider(Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PersonWithAvailability(person: People) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = person.name,
            )

            Column(
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                if (person.availability.isEmpty().not()) {
                    person.availability.forEach {
                        Row {
                            Text(it.display)
                            Text(" - ")
                            Text(it.time.getDisplayText())
                        }
                    }
                } else {
                    Text("No availability submitted (yet)")
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

/*
when saving last room code, save last user ID so we know who you were
        policy, error checking (name, time, date etc)*/
