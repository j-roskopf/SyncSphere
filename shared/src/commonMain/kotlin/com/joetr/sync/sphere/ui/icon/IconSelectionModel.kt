package com.joetr.sync.sphere.ui.icon

import cafe.adriel.voyager.core.model.ScreenModel
import com.joetr.sync.sphere.data.RoomRepository
import com.joetr.sync.sphere.ui.icon.data.IconSelection
import com.joetr.sync.sphere.ui.icon.data.ImageProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IconSelectionModel(
    private val roomRepository: RoomRepository,
) : ScreenModel {
    private val _state = MutableStateFlow<IconSelectionViewState>(IconSelectionViewState.Loading)
    val state: StateFlow<IconSelectionViewState> = _state

    fun init() {
        val selectedImage = roomRepository.getLocalIcon()
        _state.value = IconSelectionViewState.Content(
            ImageProvider.images().map {
                IconSelection(
                    image = it,
                    selected = it == selectedImage,
                )
            },
        )
    }

    fun selectImage(image: String) {
        val currentState = state.value
        if (currentState is IconSelectionViewState.Content) {
            roomRepository.saveIconLocally(image)
            _state.value = IconSelectionViewState.Content(
                images = currentState.images.map {
                    if (it.image == image) {
                        it.copy(
                            selected = true,
                        )
                    } else {
                        it.copy(
                            selected = false,
                        )
                    }
                },
            )
        }
    }
}
