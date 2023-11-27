package com.joetr.sync.sphere.ui.previous

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
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
import com.kevinnzou.swipebox.SwipeBox
import com.kevinnzou.swipebox.SwipeDirection
import com.kevinnzou.swipebox.widget.SwipeIcon
import kotlinx.coroutines.launch

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
                contentKey = {
                    it.key
                },
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
                        deleteRoom = {
                            screenModel.deleteRoom(it)
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ContentState(
        modifier: Modifier = Modifier,
        previousRooms: List<PreviousRoom>,
        navigateToRoomDetail: (String, String, String) -> Unit,
        deleteRoom: (String) -> Unit,
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            itemsIndexed(
                items = previousRooms,
                key = { _, previousRoom -> "room-${previousRoom.roomCode}" },
            ) { index, previousRoom ->
                RoomItem(
                    modifier = Modifier.animateItemPlacement(),
                    roomCode = previousRoom.roomCode,
                    navigateToRoomDetail = navigateToRoomDetail,
                    previousUserId = previousRoom.userId,
                    previousUserName = previousRoom.userName,
                    deleteRoom = deleteRoom,
                )
                if (index != previousRooms.size - 1) {
                    Divider(modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }

    @Composable
    private fun RoomItem(
        modifier: Modifier,
        roomCode: String,
        previousUserId: String,
        previousUserName: String,
        navigateToRoomDetail: (String, String, String) -> Unit,
        deleteRoom: (String) -> Unit,
    ) {
        SwipeContainer(
            modifier = modifier,
            roomCode = roomCode,
            deleteRoom = deleteRoom,
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
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
    @Composable
    fun SwipeContainer(
        modifier: Modifier,
        roomCode: String,
        deleteRoom: (String) -> Unit,
        content: @Composable () -> Unit,
    ) {
        val coroutineScope = rememberCoroutineScope()
        SwipeBox(
            modifier = modifier.fillMaxWidth(),
            swipeDirection = SwipeDirection.EndToStart,
            endContentWidth = 60.dp,
            endContent = { swipeableState, _ ->
                SwipeIcon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    background = MaterialTheme.colorScheme.errorContainer,
                    weight = 1f,
                    iconSize = 24.dp,
                ) {
                    coroutineScope.launch {
                        swipeableState.animateTo(0)
                    }
                    deleteRoom(roomCode)
                }
            },
        ) { _, _, _ ->
            content()
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
