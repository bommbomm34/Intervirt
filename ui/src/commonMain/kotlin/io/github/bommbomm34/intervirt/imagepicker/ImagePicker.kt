/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.imagepicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.pick_os
import intervirt.ui.generated.resources.pick_os_description
import io.github.bommbomm34.intervirt.appendResource
import io.github.bommbomm34.intervirt.appendResourceLine
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.data.Image
import io.github.bommbomm34.intervirt.data.Images

private val HEADLINE_STYLE = SpanStyle(
    fontSize = 32.sp,
    fontWeight = FontWeight.Bold,
)

@Composable
fun ImagePicker(onInstall: (Image) -> Unit) {
    var showImageInfo by remember { mutableStateOf(false) }
    var selectedImage: Image? by remember { mutableStateOf(null) }

    CenterColumn(Modifier.padding(top = 32.dp)) {
        Text(
            text = buildAnnotatedString {
                withStyle(HEADLINE_STYLE) {
                    appendResourceLine(Res.string.pick_os)
                }

                appendResource(Res.string.pick_os_description)
            },
        )
        LazyVerticalGrid(
            modifier = Modifier.padding(8.dp),
            columns = GridCells.FixedSize(currentAppEnv.osIconSize.dp * 1.5f),
        ) {
            items(Images.ALL_IMAGES) { image ->
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
