package com.joetr.sync.sphere.ui.new

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.common.images.MainResImages
import com.joetr.sync.sphere.data.CrashReporting
import com.joetr.sync.sphere.data.model.JoinedRoom
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.time.TimeSelectionScreen
import com.joetr.sync.sphere.util.format
import com.joetr.sync.sphere.util.iOS
import io.github.skeptick.libres.compose.painterResource
import io.github.skeptick.libres.images.Image
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.mp.KoinPlatform.getKoin

class NewRoomScreen(val joinedRoom: JoinedRoom?, val name: String) : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<NewRoomScreenModel>()
        val viewState = screenModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow
        val crashReporting = getKoin().get<CrashReporting>()

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
                navigateToTimeSelectionScreen = {
                    navigator.push(
                        TimeSelectionScreen(
                            times = it,
                            roomCode = screenModel.room?.roomCode ?: run {
                                val exception = IllegalArgumentException(
                                    "Unknown room code",
                                )
                                crashReporting.recordException(exception)

                                throw exception
                            },
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
            is NewRoomState.Error -> {
                ErrorState(
                    tryAgain = {
                        screenModel.init(
                            joinedRoom = joinedRoom,
                            name = name,
                        )
                    },
                )
            }
        }
    }

    @Composable
    private fun ErrorState(
        tryAgain: () -> Unit,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
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

                Button(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        tryAgain()
                    },
                ) {
                    Text("Try Again")
                }
            }
        }
    }

    @Composable
    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalLayoutApi::class,
    )
    private fun ContentState(
        roomCode: String,
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
                    text = "You: $name",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            ContentStateAvatars(
                names = names,
            )

            val state = rememberDatePickerState()

            NewRoomDatePicker(state = state)

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item {
                    Text("Selected Dates: ")
                }
                selectedDates.forEach {
                    item {
                        val isVisible = remember { mutableStateOf(false) }
                        LaunchedEffect(it) {
                            isVisible.value = true
                        }

                        AnimatedVisibility(
                            visible = isVisible.value,
                        ) {
                            SuggestionChip(
                                onClick = {
                                    //
                                },
                                label = {
                                    Text(it)
                                },
                                modifier = Modifier.padding(4.dp),
                            )
                        }
                    }
                }
            }

            Row {
                Button(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        val localMillis = state.selectedDateMillis
                        if (localMillis != null) {
                            val date = Instant.fromEpochMilliseconds(localMillis)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).format("MM-dd-yyyy")
                            addDate(date)
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
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun NewRoomDatePicker(state: DatePickerState) {
        DatePicker(
            dateFormatter = remember {
                DatePickerFormatter(
                    // todo joer - display month on iOS too once it's fixed https://github.com/JetBrains/compose-multiplatform/issues/3903#issuecomment-1794017166
                    yearSelectionSkeleton = if (iOS) "yyyy" else "MMM yyyy",
                )
            },
            state = state,
            title = null,
            headline = {
                // todo joer, hopefully just use default - https://github.com/JetBrains/compose-multiplatform/issues/3903

                val formattedValue = if (state.selectedDateMillis == null) {
                    "Select a date"
                } else {
                    Instant.fromEpochMilliseconds(state.selectedDateMillis!!).toLocalDateTime(TimeZone.UTC)
                        .format("MMM dd, yyyy")
                }

                Text(
                    text = formattedValue,
                    modifier = Modifier.padding(PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp)),
                    maxLines = 1,
                )
            },
            showModeToggle = false,
        )
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ContentStateAvatars(names: List<String>) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            names.forEachIndexed { index, name ->
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = getImageDataForPosition(index).painterResource(),
                        contentDescription = "Icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                            .padding(8.dp) // padding between border and image
                            .size(64.dp),

                    )
                    Text(name)
                }
            }
        }
    }

    private fun getImageDataForPosition(index: Int): Image {
        val listOfImages = listOf(
            MainResImages.dog1,
            MainResImages.dog2,
            MainResImages.dog3,
            MainResImages.cat1,
            MainResImages.cat2,
            MainResImages.cat3,
            MainResImages.dog4,
            MainResImages.dog5,
            MainResImages.dog6,
        )
        return listOfImages[index % listOfImages.size]
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
