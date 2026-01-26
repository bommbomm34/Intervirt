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
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.CloseButton
import org.koin.compose.koinInject

@Composable
fun ImagePicker(
    onDismiss: () -> Unit,
    onInstall: (Image) -> Unit
) {
    val preferences = koinInject<Preferences>()
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
    AlignedBox(Alignment.Center, 64.dp){
        LazyVerticalGrid(
            columns = GridCells.FixedSize(preferences.OS_ICON_SIZE * 1.2f)
        ){
            items(images){ image ->
                ImageItem(image){
                    showImageInfo = true
                    selectedImage = image
                }
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