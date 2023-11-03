package com.joetr.sync.sphere.ui.pre

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.joetr.sync.sphere.ui.new.NewRoomScreen
import com.joetr.sync.sphere.ui.results.ResultsScreen
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class PreRoomScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<PreScreenModel>()
        var showRoomDoesNotExistError by remember { mutableStateOf(false) }
        val roomCodeText = remember { mutableStateOf("") }
        val nameText = remember { mutableStateOf("") }

        LifecycleEffect(
            onStarted = {
                screenModel.init()
            },
        )

        screenModel.action.collectAsEffect {
            when (it) {
                is PreScreenActions.RoomDoesNotExist -> {
                    showRoomDoesNotExistError = true
                }

                is PreScreenActions.RoomExists -> screenModel.joinRoom(it.roomCode, it.name)
                is PreScreenActions.NavigateToRoom -> {
                    val name = nameText.value.ifEmpty { screenModel.getAnonymousUsername() }
                    navigator.push(
                        NewRoomScreen(
                            joinedRoom = JoinedRoom(
                                room = it.room,
                                id = it.userId,
                            ),
                            name = name,
                        ),
                    )
                }
            }
        }

        when (val state = screenModel.state.collectAsState().value) {
            is PreScreenViewState.Content -> {
                when {
                    showRoomDoesNotExistError -> RoomDoesNotExistErrorDialog(
                        onDismiss = {
                            showRoomDoesNotExistError = false
                        },
                        tryAgain = {
                            val name = nameText.value.ifEmpty { screenModel.getAnonymousUsername() }
                            screenModel.validateRoomCode(roomCodeText.value, name)
                        },
                    )

                    else -> ContentState(
                        goToNewRoomScreen = {
                            val name = nameText.value.ifEmpty { screenModel.getAnonymousUsername() }
                            navigator.push(NewRoomScreen(joinedRoom = null, name = name))
                        },
                        validateRoomCode = {
                            val name = nameText.value.ifEmpty { screenModel.getAnonymousUsername() }

                            screenModel.validateRoomCode(
                                roomCode = roomCodeText.value,
                                name = name,
                            )
                        },
                        roomCodeText = roomCodeText.value,
                        onRoomCodeTextChange = {
                            roomCodeText.value = it
                        },
                        nameText = nameText.value,
                        onNameTextChange = {
                            nameText.value = it
                        },
                        lastKnownRoomCode = state.lastKnownRoomCode,
                        goToResultsForLastKnownRoom = {
                            navigator.push(
                                ResultsScreen(
                                    roomCode = it,
                                ),
                            )
                        },
                    )
                }
            }

            is PreScreenViewState.Loading -> LoadingState()
        }
    }

    @Composable
    private fun RoomDoesNotExistErrorDialog(
        onDismiss: () -> Unit,
        tryAgain: () -> Unit,
    ) {
        AdaptiveAlertDialog(
            title = "Error",
            text = "Room does not exist",
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

    @Composable
    @Suppress("LongParameterList")
    private fun ContentState(
        goToNewRoomScreen: () -> Unit,
        validateRoomCode: () -> Unit,
        goToResultsForLastKnownRoom: (String) -> Unit,
        roomCodeText: String,
        onRoomCodeTextChange: (String) -> Unit,
        nameText: String,
        onNameTextChange: (String) -> Unit,
        lastKnownRoomCode: String?,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = nameText,
                label = {
                    Text("Your name (optional)")
                },
                onValueChange = {
                    onNameTextChange(it)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
            )

            Button({
                goToNewRoomScreen()
            }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Room")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = roomCodeText,
                    label = {
                        Text("Room Code")
                    },
                    onValueChange = {
                        onRoomCodeTextChange(it)
                    },
                    shape = RoundedCornerShape(12.dp),
                )

                IconButton(
                    onClick = {
                        validateRoomCode()
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                    content = {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                        )
                    },
                )
            }

            if (lastKnownRoomCode != null) {
                Button({
                    goToResultsForLastKnownRoom(lastKnownRoomCode)
                }) {
                    Text("View Results From Last Known Room")
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

@Composable
fun <T> Flow<T>.collectAsEffect(
    context: CoroutineContext = EmptyCoroutineContext,
    block: (T) -> Unit,
) {
    LaunchedEffect(key1 = Unit) {
        onEach(block).flowOn(context).launchIn(this)
    }
}
