package io.github.bommbomm34.intervirt.gui.imagepicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.data.Image
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton

@Composable
fun ImagePicker(
    onDismiss: () -> Unit,
    onInstall: (Image) -> Unit
) {
    val images = remember { mutableStateListOf<Image>() }
    var showImageInfo by remember { mutableStateOf(false) }
    var selectedImage: Image? by remember { mutableStateOf(null) }
    AlignedBox(Alignment.TopStart){
        CloseButton(onDismiss)
    }
    LaunchedEffect(Unit){
        images.clear()
        images.addAll(Image.getImages())
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp)
    ){
        items(images){ image -> // Incus Image, not a photo :)
            ImageItem(image){
                showImageInfo = true
                selectedImage = image
            }
        }
    }
    AnimatedVisibility(showImageInfo){
        selectedImage?.let {
            ImageInfo(
                image = it,
                onDismiss = { showImageInfo = false },
                onInstall = { onInstall(it) }
            )
        }
    }
}