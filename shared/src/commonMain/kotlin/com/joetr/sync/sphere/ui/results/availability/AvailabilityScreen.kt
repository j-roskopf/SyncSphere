package com.joetr.sync.sphere.ui.results.availability

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.data.model.Finalization
import com.joetr.sync.sphere.data.model.People
import com.joetr.sync.sphere.design.button.PrimaryButton
import com.joetr.sync.sphere.design.button.debouncedClick
import com.joetr.sync.sphere.design.theme.conditional
import com.joetr.sync.sphere.design.toolbar.DefaultToolbar
import com.joetr.sync.sphere.design.toolbar.backOrNull
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.pre.collectAsEffect
import com.joetr.sync.sphere.ui.results.availability.data.DayStatus
import com.joetr.sync.sphere.ui.time.DayTime
import com.joetr.sync.sphere.ui.time.getDisplayText
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import syncsphere.shared.generated.resources.Res
import syncsphere.shared.generated.resources.full_availability
import syncsphere.shared.generated.resources.no_availability
import syncsphere.shared.generated.resources.partial_availability

class AvailabilityScreen(
    val data: Map<String, DayTime>,
    val person: People,
    val roomCode: String,
) : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<AvailabilityScreenModel>()
        val viewState = screenModel.state.collectAsState().value

        LifecycleEffect(
            onStarted = {
                screenModel.init(
                    inputData = data,
                    roomCode = roomCode,
                    person = person,
                )
            },
        )

        val displayErrorDialog = remember { mutableStateOf(false) }

        screenModel.action.collectAsEffect {
            when (it) {
                AvailabilityScreenAction.AddToCalendarError -> displayErrorDialog.value = true
            }
        }

        if (displayErrorDialog.value) {
            ErrorDialog(
                onDismiss = {
                    displayErrorDialog.value = false
                },
            )
        }

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
                    is AvailabilityScreenState.Content -> AvailabilityState(
                        modifier = Modifier.padding(paddingValues),
                        data = targetState.data,
                        finalizations = targetState.finalizations,
                        addToCalendar = { localDate, dayTime ->
                            screenModel.addToCalendar(localDate, dayTime)
                        },
                        finalize = { localDate, dayTime ->
                            screenModel.finalize(person, roomCode, localDate, dayTime)
                        },
                        namesThatNeedToFinalize = targetState.namesThatNeedToFinalize,
                        hasUserSubmittedFinalization = targetState.hasUserSubmittedFinalization,
                        undoFinalization = {
                            screenModel.undoFinalization(
                                person = person,
                                roomCode = roomCode,
                            )
                        },
                        finalDate = targetState.finalDate,
                    )

                    is AvailabilityScreenState.Loading -> LoadingState(
                        modifier = Modifier.padding(
                            paddingValues,
                        ),
                    )

                    AvailabilityScreenState.Error -> ErrorState(
                        modifier = Modifier.padding(
                            paddingValues,
                        ),
                    )
                }
            }
        }
    }

    @Composable
    @Suppress("LongParameterList")
    private fun AvailabilityState(
        modifier: Modifier = Modifier,
        data: Map<DayStatus, List<Pair<String, DayTime>>>,
        addToCalendar: (LocalDate, DayTime) -> Unit,
        finalize: (LocalDate, DayTime) -> Unit,
        undoFinalization: () -> Unit,
        finalizations: Map<String, List<Finalization>>,
        namesThatNeedToFinalize: Map<String, List<String>>,
        hasUserSubmittedFinalization: Boolean,
        finalDate: String?,
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                item {
                    val text = if (finalDate != null) {
                        "Finalized Date: $finalDate"
                    } else if (hasUserSubmittedFinalization) {
                        "You Have Finalized A Date!"
                    } else {
                        "Finalize A Date"
                    }
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = text,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                val daysDoWorkEntry = data[DayStatus.DAY_WORKS]!!

                daysDoWorkEntry.onEachIndexed { index, entry ->
                    item {
                        AvailabilityDayItem(
                            displayDay = entry.first,
                            dayTime = entry.second,
                            addToCalendar = addToCalendar,
                            finalize = finalize,
                            finalizations = finalizations,
                            namesThatNeedToFinalize = namesThatNeedToFinalize,
                            hasUserSubmittedFinalization = hasUserSubmittedFinalization,
                        )

                        if (index != daysDoWorkEntry.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }

                val daysDoNotWorkEntry = data[DayStatus.DAY_DOES_NOT_WORK]!!
                if (daysDoNotWorkEntry.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = "Days That Do Not Work For Everyone",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }
                daysDoNotWorkEntry.onEachIndexed { index, entry ->
                    item {
                        AvailabilityDayItem(
                            displayDay = entry.first,
                            dayTime = entry.second,
                            addToCalendar = addToCalendar,
                            finalize = finalize,
                            finalizations = finalizations,
                            namesThatNeedToFinalize = namesThatNeedToFinalize,
                            hasUserSubmittedFinalization = hasUserSubmittedFinalization,
                        )

                        if (index != daysDoNotWorkEntry.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = hasUserSubmittedFinalization,
            ) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                PrimaryButton(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    onClick = debouncedClick {
                        undoFinalization()
                    },
                ) {
                    Text("Undo Finalization")
                }
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class, ExperimentalLayoutApi::class)
    @Composable
    @Suppress("MagicNumber", "LongMethod", "CyclomaticComplexMethod")
    private fun AvailabilityDayItem(
        displayDay: String,
        dayTime: DayTime,
        addToCalendar: (LocalDate, DayTime) -> Unit,
        finalize: (LocalDate, DayTime) -> Unit,
        finalizations: Map<String, List<Finalization>>,
        namesThatNeedToFinalize: Map<String, List<String>>,
        hasUserSubmittedFinalization: Boolean,
    ) {
        val coroutineScope = rememberCoroutineScope()
        val isActionRowVisible = remember { mutableStateOf(false) }
        val shouldDisplayActionRow = dayTime is DayTime.AllDay || dayTime is DayTime.Range

        val arrowRotation = remember { Animatable(0f) }

        Column(
            modifier = Modifier.fillMaxWidth()
                .conditional(
                    shouldDisplayActionRow,
                    {
                        Modifier.clickable {
                            isActionRowVisible.value = isActionRowVisible.value.not()

                            coroutineScope.launch {
                                if (isActionRowVisible.value) {
                                    arrowRotation.animateTo(90f)
                                } else {
                                    arrowRotation.animateTo(00f)
                                }
                            }
                        }
                    },
                )
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val image = when (dayTime) {
                    is DayTime.AllDay -> Res.drawable.full_availability
                    is DayTime.NotSelected -> Res.drawable.no_availability
                    is DayTime.Range -> Res.drawable.partial_availability
                }

                if (namesThatNeedToFinalize[displayDay]?.isEmpty() == true && shouldDisplayActionRow) {
                    // everyone has finalized
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Image(
                        painter = painterResource(image),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.size(8.dp))

                Column(
                    modifier = Modifier.weight(1.5f),
                ) {
                    Text(
                        text = displayDay,
                    )
                    if (finalizations.containsKey(displayDay)) {
                        val size = finalizations[displayDay]!!.size
                        val qualifierText = if (size == 1) "vote" else "votes"
                        if (finalizations[displayDay]?.isNotEmpty() == true) {
                            Text("$size $qualifierText")
                        }
                    }
                }

                val text = when (dayTime) {
                    is DayTime.AllDay -> "This day works for everyone all day"
                    is DayTime.NotSelected -> "No availability on day"
                    is DayTime.Range -> "This day works for everyone between ${dayTime.getDisplayText()}"
                }

                Text(
                    modifier = Modifier.weight(3f),
                    text = text,
                )

                if (shouldDisplayActionRow) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.graphicsLayer {
                            rotationZ = arrowRotation.value
                        },
                    )
                }
            }
            if (shouldDisplayActionRow) {
                AnimatedVisibility(
                    visible = isActionRowVisible.value,
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        FlowRow {
                            PrimaryButton(
                                modifier = Modifier.padding(8.dp),
                                onClick = debouncedClick {
                                    val availability = LocalDate.parse(displayDay)
                                    addToCalendar(availability, dayTime)
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open Calendar")
                            }

                            if (hasUserSubmittedFinalization.not()) {
                                PrimaryButton(
                                    modifier = Modifier.padding(8.dp),
                                    onClick = debouncedClick {
                                        val availability = LocalDate.parse(displayDay)
                                        finalize(availability, dayTime)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Finalize")
                                }
                            }
                        }

                        val textModifier = Modifier.padding(horizontal = 8.dp)
                        if (finalizations.containsKey(displayDay)) {
                            if (namesThatNeedToFinalize[displayDay]?.isEmpty() == true) {
                                Text(modifier = textModifier, text = "Finalized by everyone")
                            } else {
                                Text(
                                    modifier = textModifier,
                                    text = "Finalized by: ${
                                        finalizations[displayDay]!!.joinToString(separator = ", ") {
                                            it.person.name
                                        }
                                    }",
                                )
                            }

                            if (namesThatNeedToFinalize[displayDay]?.isNotEmpty() == true) {
                                Text(
                                    text = namesThatNeedToFinalize[displayDay]!!.joinToString(
                                        separator = ", ",
                                    ) + " still need to finalize.",
                                    modifier = textModifier,
                                )
                            }
                        } else {
                            Text(
                                text = "Date has not been finalized by anyone",
                                modifier = textModifier,
                            )
                        }
                    }
                }
            }
        }
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
    private fun ErrorDialog(
        onDismiss: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                Text("An error occurred")
            },
            confirmButton = {
                PrimaryButton(
                    onClick = {
                        onDismiss()
                    },
                ) {
                    Text("Okay")
                }
            },
        )
    }
}
