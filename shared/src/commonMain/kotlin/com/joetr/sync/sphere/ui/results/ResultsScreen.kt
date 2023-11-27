package com.joetr.sync.sphere.ui.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.data.model.JoinedRoom
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.data.model.Room
import com.joetr.sync.sphere.design.button.PrimaryButton
import com.joetr.sync.sphere.design.button.SecondaryButton
import com.joetr.sync.sphere.design.toolbar.DefaultToolbar
import com.joetr.sync.sphere.design.toolbar.backOrNull
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.new.NewRoomScreen
import com.joetr.sync.sphere.ui.pre.collectAsEffect
import com.joetr.sync.sphere.ui.results.availability.AvailabilityScreen
import com.joetr.sync.sphere.ui.time.getDisplayText

/**
 * @param previousUserId - user id used when room was saved.
 * if not null, allows user to add their availability if they have not already submitted
 * @param previousUserName - name used when room was saved.
 * if not null, allows user to add their availability if they have not already submitted
 */
class ResultsScreen(
    val roomCode: String,
    private val previousUserId: String?,
    private val previousUserName: String?,
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
                    navigator.push(AvailabilityScreen(data = it.timeRanges, person = it.person, roomCode = roomCode))
                }
            }
        }

        Scaffold(
            topBar = {
                DefaultToolbar(
                    onBack = LocalNavigator.currentOrThrow.backOrNull(),
                )
            },
        ) { paddingValues ->
            when (viewState) {
                is ResultsScreenState.Content -> ContentState(
                    modifier = Modifier.padding(paddingValues),
                    room = viewState.room,
                    calculateAvailability = {
                        screenModel.calculateAvailability(it, roomCode)
                    },
                    hasUserSubmittedAvailability = viewState.hasUserSubmittedAvailability,
                    onSubmitAvailability = { room, previousUserId, previousUserName ->
                        navigator.push(
                            NewRoomScreen(
                                joinedRoom = JoinedRoom(
                                    room = room,
                                    id = previousUserId,
                                ),
                                previousUserName,
                            ),
                        )
                    },
                )

                is ResultsScreenState.Loading -> LoadingState(
                    modifier = Modifier.padding(paddingValues),
                )

                is ResultsScreenState.Error -> ErrorState(
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }

    @Composable
    private fun ErrorState(
        modifier: Modifier = Modifier,
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    style = MaterialTheme.typography.displayMedium,
                    text = "Something went wrong \uD83D\uDE41",
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    @Composable
    private fun ContentState(
        modifier: Modifier = Modifier,
        room: Room,
        calculateAvailability: (List<People>) -> Unit,
        hasUserSubmittedAvailability: Boolean,
        onSubmitAvailability: (Room, String, String) -> Unit,
    ) {
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                item {
                    Text(
                        text = "Code: $roomCode",
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

            if (hasUserSubmittedAvailability.not() && previousUserId != null && previousUserName != null) {
                SecondaryButton(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    onClick = {
                        onSubmitAvailability(room, previousUserId, previousUserName)
                    },
                ) {
                    Text("Add Availability")
                }
            }

            if (hasUserSubmittedAvailability && previousUserId != null && previousUserName != null) {
                SecondaryButton(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    onClick = {
                        onSubmitAvailability(room, previousUserId, previousUserName)
                    },
                ) {
                    Text("Edit Availability")
                }
            }

            AnimatedVisibility(
                visible = room.people.any { person ->
                    person.availability.isNotEmpty()
                },
            ) {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    onClick = {
                        calculateAvailability(room.people)
                    },
                ) {
                    Text("Calculate availability")
                }
            }
        }
    }

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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
    private fun LoadingState(
        modifier: Modifier = Modifier,
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ProgressIndicator()
        }
    }
}
