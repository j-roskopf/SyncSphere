package com.joetr.sync.sphere.ui.icon

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.joetr.sync.sphere.design.toolbar.DefaultToolbar
import com.joetr.sync.sphere.design.toolbar.backOrNull
import com.joetr.sync.sphere.ui.ProgressIndicator
import com.joetr.sync.sphere.ui.icon.data.IconSelection
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class IconSelectionScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<IconSelectionModel>()
        val state = screenModel.state.collectAsState().value

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
                    is IconSelectionViewState.Content -> ContentState(
                        modifier = Modifier.padding(paddingValues),
                        images = targetState.images,
                        imageSelected = {
                            screenModel.selectImage(it)
                        },
                    )

                    is IconSelectionViewState.Loading -> LoadingState(
                        modifier = Modifier.padding(paddingValues),
                    )
                }
            }
        }
    }

    @Composable
    private fun ContentState(
        modifier: Modifier = Modifier,
        images: List<IconSelection>,
        imageSelected: (DrawableResource) -> Unit,
    ) {
        LazyVerticalGrid(
            modifier = modifier.fillMaxSize(),
            columns = GridCells.Adaptive(minSize = 128.dp),
        ) {
            items(images) { image ->
                ImageItem(
                    image = image,
                    imageSelected = imageSelected,
                )
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    private fun ImageItem(image: IconSelection, imageSelected: (DrawableResource) -> Unit) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(image.image),
                contentDescription = "Icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(8.dp)
                    .size(96.dp)
                    .clickable {
                        imageSelected(image.image)
                    },
            )

            if (image.selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
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
}
