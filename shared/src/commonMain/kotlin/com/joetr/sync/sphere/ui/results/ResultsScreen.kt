package com.joetr.sync.sphere.ui.results

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
                modifier = Modifier.weight(1f),
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

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    calculateAvailability(room.people)
                },
            ) {
                Text("Calculate availability")
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun PersonWithAvailability(person: People) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = person.name,
                style = MaterialTheme.typography.displaySmall,
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
            ) {
                if (person.availability.isEmpty().not()) {
                    person.availability.forEach {
                        item {
                            Card(
                                modifier = Modifier.padding(4.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = it.display,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(4.dp),
                                    )
                                    Text(
                                        text = it.time.getDisplayText(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(4.dp),
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text("No availability submitted (yet)")
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
