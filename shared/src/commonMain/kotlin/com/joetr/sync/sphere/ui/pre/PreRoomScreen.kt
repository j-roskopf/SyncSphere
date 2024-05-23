package com.joetr.sync.sphere.ui.pre

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.data.model.JoinedRoom
import com.joetr.sync.sphere.design.button.PrimaryButton
import com.joetr.sync.sphere.design.button.debouncedClick
import com.joetr.sync.sphere.design.theme.conditional
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.icon.IconSelectionScreen
import com.joetr.sync.sphere.ui.icon.data.toDrawableRes
import com.joetr.sync.sphere.ui.new.NewRoomScreen
import com.joetr.sync.sphere.ui.previous.PreviousRoomScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import syncsphere.shared.generated.resources.Res
import syncsphere.shared.generated.resources.desktop_icon
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class PreRoomScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<PreScreenModel>()
        var showRoomDoesNotExistError by remember { mutableStateOf(false) }
        val roomCodeText = remember { mutableStateOf("") }

        val state = screenModel.state.collectAsState().value

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

                is PreScreenActions.RoomExists -> screenModel.joinRoom(
                    it.roomCode,
                    it.name,
                )

                is PreScreenActions.NavigateToRoom -> {
                    navigator.push(
                        NewRoomScreen(
                            joinedRoom = JoinedRoom(
                                room = it.room,
                                id = it.userId,
                            ),
                            name = it.name,
                        ),
                    )
                }
            }
        }

        if (showRoomDoesNotExistError) {
            RoomDoesNotExistErrorDialog(
                onDismiss = {
                    showRoomDoesNotExistError = false
                },
                tryAgain = {
                    showRoomDoesNotExistError = false
                    screenModel.tryAgain(
                        roomCode = roomCodeText.value,
                    )
                },
            )
        }

        when (state) {
            is PreScreenViewState.Content -> {
                val nameText = remember {
                    mutableStateOf(
                        state.lastKnownName,
                    )
                }
                ContentState(
                    goToNewRoomScreen = {
                        val name = nameText.value.ifEmpty {
                            screenModel.getAnonymousUsername()
                        }

                        navigator.push(
                            NewRoomScreen(
                                joinedRoom = null,
                                name = name,
                            ),
                        )
                    },
                    validateRoomCode = {
                        val name = nameText.value.ifEmpty {
                            screenModel.getAnonymousUsername()
                        }

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
                    goToPreviousRoomsScreen = {
                        navigator.push(
                            PreviousRoomScreen(),
                        )
                    },
                    goToIconSelectionScreen = {
                        navigator.push(
                            IconSelectionScreen(),
                        )
                    },
                    userPreferenceIcon = state.userIconPreference.toDrawableRes(),
                )
            }

            is PreScreenViewState.Loading -> LoadingState()
        }
    }

    @Composable
    private fun RoomDoesNotExistErrorDialog(
        onDismiss: () -> Unit,
        tryAgain: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            confirmButton = {
                PrimaryButton(onClick = {
                    tryAgain()
                }) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                PrimaryButton(onClick = {
                    onDismiss()
                }) {
                    Text("Okay")
                }
            },
            title = {
                Text("Error")
            },
            text = {
                Text("Room does not exist")
            },
        )
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
    @Composable
    @Suppress("LongParameterList", "LongMethod")
    private fun ContentState(
        goToNewRoomScreen: () -> Unit,
        goToPreviousRoomsScreen: () -> Unit,
        validateRoomCode: () -> Unit,
        roomCodeText: String,
        onRoomCodeTextChange: (String) -> Unit,
        nameText: String,
        onNameTextChange: (String) -> Unit,
        goToIconSelectionScreen: () -> Unit,
        userPreferenceIcon: DrawableResource?,
    ) {
        val focusManager = LocalFocusManager.current
        val coroutineScope = rememberCoroutineScope()
        val bringIntoViewRequester = remember { BringIntoViewRequester() }

        Column(
            modifier = Modifier.fillMaxSize().onFocusEvent { state ->
                if (state.hasFocus || state.isFocused) {
                    coroutineScope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            }.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    goToIconSelectionScreen()
                },
            ) {
                Box {
                    Image(
                        painter = painterResource(
                            userPreferenceIcon ?: Res.drawable.desktop_icon,
                        ),
                        contentDescription = "Icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .conditional(userPreferenceIcon == null, {
                                clip(CircleShape)
                            })
                            .size(128.dp),
                    )
                    Icon(
                        modifier = Modifier
                            .align(Alignment.BottomEnd),
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (userPreferenceIcon == null) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Change Avatar",
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                )

                PrimaryButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = debouncedClick {
                        goToNewRoomScreen()
                    },
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Room")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f).onFocusEvent { state ->
                        if (state.hasFocus || state.isFocused) {
                            coroutineScope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                    value = roomCodeText,
                    label = {
                        Text("Room Code")
                    },
                    onValueChange = {
                        onRoomCodeTextChange(it)
                    },
                    shape = RoundedCornerShape(12.dp),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                )

                IconButton(
                    onClick = {
                        validateRoomCode()
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                    content = {
                        Icon(
                            Icons.AutoMirrored.Default.Send,
                            contentDescription = null,
                        )
                    },
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Create a room and share the room code with friends to schedule your next get-together!",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                )
                PrimaryButton(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                    onClick = debouncedClick {
                        goToPreviousRoomsScreen()
                    },
                ) {
                    Text("View All Previous Rooms")
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
