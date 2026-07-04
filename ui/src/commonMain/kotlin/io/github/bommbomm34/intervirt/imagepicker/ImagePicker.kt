/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.imagepicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.IMAGES
import io.github.bommbomm34.intervirt.data.Image
import org.koin.compose.koinInject

@Composable
fun ImagePicker(
    onDismiss: () -> Unit,
    onInstall: (Image) -> Unit,
) {
    var showImageInfo by remember { mutableStateOf(false) }
    var selectedImage: Image? by remember { mutableStateOf(null) }
    AlignedBox(Alignment.TopStart) {
        CloseButton(onDismiss)
    }
    AlignedBox(Alignment.Center, 64.dp) {
        LazyVerticalGrid(
            columns = GridCells.FixedSize(currentAppEnv.osIconSize.dp * 1.5f),
        ) {
            items(IMAGES) { image ->
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
