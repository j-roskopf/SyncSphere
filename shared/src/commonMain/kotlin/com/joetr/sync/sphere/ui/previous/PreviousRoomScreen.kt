package com.joetr.sync.sphere.ui.previous

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.design.toolbar.DefaultToolbar
import com.joetr.sync.sphere.design.toolbar.backOrNull
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.previous.data.PreviousRoom
import com.joetr.sync.sphere.ui.results.ResultsScreen

class PreviousRoomScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<PreviousScreenModel>()
        val state = screenModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow

        LifecycleEffect(
            onStarted = {
                screenModel.init()
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
                targetState = state,
            ) { targetState ->
                when (targetState) {
                    is PreviousScreenViewState.Content -> ContentState(
                        modifier = Modifier.padding(paddingValues),
                        targetState.previousRooms,
                        navigateToRoomDetail = { roomCode, previousUserId, previousUserName ->
                            navigator.push(
                                ResultsScreen(
                                    roomCode = roomCode,
                                    previousUserId = previousUserId,
                                    previousUserName = previousUserName,
                                ),
                            )
                        },
                    )

                    is PreviousScreenViewState.Error -> ErrorState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is PreviousScreenViewState.Loading -> LoadingState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is PreviousScreenViewState.Empty -> EmptyState(
                        modifier = Modifier.padding(paddingValues),
                    )
                }
            }
        }
    }

    @Composable
    private fun ContentState(
        modifier: Modifier = Modifier,
        previousRooms: List<PreviousRoom>,
        navigateToRoomDetail: (String, String, String) -> Unit,
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            previousRooms.forEachIndexed { index, previousRoom ->
                item {
                    RoomItem(
                        roomCode = previousRoom.roomCode,
                        navigateToRoomDetail = navigateToRoomDetail,
                        previousUserId = previousRoom.userId,
                        previousUserName = previousRoom.userName,
                    )
                    if (index != previousRooms.size - 1) {
                        Divider(modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun RoomItem(
        roomCode: String,
        previousUserId: String,
        previousUserName: String,
        navigateToRoomDetail: (String, String, String) -> Unit,
    ) {
        Text(
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .fillMaxWidth()
                .clickable {
                    navigateToRoomDetail(roomCode, previousUserId, previousUserName)
                }
                .padding(8.dp),
            text = "Code: $roomCode",
            style = MaterialTheme.typography.headlineMedium,
        )
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
    private fun EmptyState(
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
                    style = MaterialTheme.typography.displaySmall,
                    text = "You haven't visited any rooms yet!",
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
