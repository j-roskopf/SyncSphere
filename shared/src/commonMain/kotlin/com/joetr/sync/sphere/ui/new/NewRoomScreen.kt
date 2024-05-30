package com.joetr.sync.sphere.ui.new

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.joetr.sync.sphere.crash.CrashReporting
import com.joetr.sync.sphere.data.model.JoinedRoom
import com.joetr.sync.sphere.design.button.PrimaryButton
import com.joetr.sync.sphere.design.button.debouncedClick
import com.joetr.sync.sphere.design.toolbar.DefaultToolbar
import com.joetr.sync.sphere.design.toolbar.backOrNull
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.icon.data.ImageProvider
import com.joetr.sync.sphere.ui.icon.data.toDrawableRes
import com.joetr.sync.sphere.ui.time.TimeSelectionScreen
import epicarchitect.calendar.compose.basis.EpicMonth
import epicarchitect.calendar.compose.basis.config.rememberBasisEpicCalendarConfig
import epicarchitect.calendar.compose.datepicker.EpicDatePicker
import epicarchitect.calendar.compose.datepicker.config.rememberEpicDatePickerConfig
import epicarchitect.calendar.compose.datepicker.state.EpicDatePickerState
import epicarchitect.calendar.compose.datepicker.state.rememberEpicDatePickerState
import epicarchitect.calendar.compose.pager.config.rememberEpicCalendarPagerConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.mp.KoinPlatform.getKoin

private const val MAX_YEAR_TO_DISPLAY = 2100

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

        Scaffold(
            topBar = {
                DefaultToolbar(
                    onBack = LocalNavigator.currentOrThrow.backOrNull(),
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
                    is NewRoomState.Content -> ContentState(
                        modifier = Modifier.padding(paddingValues),
                        roomCode = targetState.roomCode,
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
                        addDates = {
                            screenModel.addDates(it)
                        },
                        selectedDates = targetState.dates,
                        names = targetState.names,
                        userPreferenceIcon = targetState.userPreferenceIcon.toDrawableRes(),
                    )

                    is NewRoomState.Loading -> LoadingState(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is NewRoomState.Error -> {
                        ErrorState(
                            modifier = Modifier.padding(paddingValues),
                            tryAgain = {
                                screenModel.init(
                                    joinedRoom = joinedRoom,
                                    name = name,
                                )
                            },
                            error = targetState.message,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ErrorState(
        modifier: Modifier = Modifier,
        tryAgain: () -> Unit,
        error: String,
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
                    text = "Something went wrong $error",
                    textAlign = TextAlign.Center,
                )

                PrimaryButton(
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
    private fun ContentState(
        modifier: Modifier = Modifier,
        roomCode: String,
        names: List<String>,
        navigateToTimeSelectionScreen: (List<String>) -> Unit,
        addDates: (List<LocalDate>) -> Unit,
        selectedDates: List<LocalDate>,
        userPreferenceIcon: DrawableResource?,
    ) {
        Column(
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Code: $roomCode",
                    style = MaterialTheme.typography.headlineMedium,
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                Text(
                    text = "You: $name",
                    style = MaterialTheme.typography.headlineSmall,
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            ContentStateAvatars(
                names = names,
                userPreferenceIcon = userPreferenceIcon,
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            DatePicker(
                selectedDates = selectedDates,
            ) {
                addDates(it)
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = selectedDates.isNotEmpty(),
            ) {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = debouncedClick {
                        // go to time selection screen
                        navigateToTimeSelectionScreen(
                            selectedDates.map {
                                it.toString()
                            },
                        )
                    },
                ) {
                    Text("Select Times")
                }
            }
        }
    }

    @Composable
    private fun DatePicker(
        selectedDates: List<LocalDate>,
        onSelectedDates: (List<LocalDate>) -> Unit,
    ) {
        val state = rememberEpicDatePickerState(
            selectionMode = EpicDatePickerState.SelectionMode.Single(maxSize = Int.MAX_VALUE),
            monthRange = monthRange(),
            config = rememberEpicDatePickerConfig(
                pagerConfig = rememberEpicCalendarPagerConfig(
                    basisConfig = rememberBasisEpicCalendarConfig(
                        displayDaysOfAdjacentMonths = false,
                        columnWidth = 42.dp,
                    ),
                ),
                selectionContentColor = MaterialTheme.colorScheme.onPrimary,
                selectionContainerColor = MaterialTheme.colorScheme.primary,
            ),
            selectedDates = selectedDates,
        )
        Column(
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp).alignByBaseline(),
                    text = state.pagerState.currentMonth.month.name,
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = state.pagerState.currentMonth.year.toString(),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            EpicDatePicker(
                state = state,
                dayOfWeekContent = {
                    val text = when (it) {
                        DayOfWeek.MONDAY -> "M"
                        DayOfWeek.TUESDAY -> "Tu"
                        DayOfWeek.WEDNESDAY -> "W"
                        DayOfWeek.THURSDAY -> "Th"
                        DayOfWeek.FRIDAY -> "F"
                        DayOfWeek.SATURDAY -> "Sa"
                        DayOfWeek.SUNDAY -> "Su"
                        else -> throw IllegalArgumentException("What day of week is this?")
                    }
                    Text(text)
                },
            )
        }

        LaunchedEffect(state.selectedDates) {
            // dates changed
            onSelectedDates(state.selectedDates)
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ContentStateAvatars(names: List<String>, userPreferenceIcon: DrawableResource?) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            names.forEachIndexed { index, name ->
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = if (this@NewRoomScreen.name == name) {
                            painterResource(
                                // use user preference icon if available
                                userPreferenceIcon ?: getImageDataForPosition(index),
                            )
                        } else {
                            painterResource(
                                getImageDataForPosition(index),
                            )
                        },
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

    private fun getImageDataForPosition(index: Int): DrawableResource {
        val listOfImages = ImageProvider.images()
        return listOfImages[index % listOfImages.size]
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

    private fun monthRange(): ClosedRange<EpicMonth> {
        val localDateNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return EpicMonth(localDateNow.year, localDateNow.month)..EpicMonth(
            MAX_YEAR_TO_DISPLAY,
            Month.DECEMBER,
        )
    }
}
