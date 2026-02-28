package io.github.bommbomm34.intervirt.imagepicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.Image
import io.github.bommbomm34.intervirt.data.getImages
import io.ktor.client.*
import org.koin.compose.koinInject

@Composable
fun ImagePicker(
    onDismiss: () -> Unit,
    onInstall: (Image) -> Unit,
) {
    val appEnv = koinInject<AppEnv>()
    val client = koinInject<HttpClient>()
    val images = remember { mutableStateListOf<Image>() }
    var showImageInfo by remember { mutableStateOf(false) }
    var selectedImage: Image? by remember { mutableStateOf(null) }
    AlignedBox(Alignment.TopStart) {
        CloseButton(onDismiss)
    }
    CatchingLaunchedEffect {
        images.clear()
        images.addAll(client.getImages(appEnv.IMAGES_URL).getOrThrow())
    }
    AlignedBox(Alignment.Center, 64.dp) {
        LazyVerticalGrid(
            columns = GridCells.FixedSize(appEnv.OS_ICON_SIZE.dp * 1.5f),
        ) {
            items(images) { image ->
                ImageItem(image) {
                    showImageInfo = true
                    selectedImage = image
                }
            }
        }
    }
    AnimatedVisibility(showImageInfo) {
        selectedImage?.let {
            ImageInfo(
                image = it,
                onDismiss = { showImageInfo = false },
                onInstall = { onInstall(it) },
            )
        }
    }
}