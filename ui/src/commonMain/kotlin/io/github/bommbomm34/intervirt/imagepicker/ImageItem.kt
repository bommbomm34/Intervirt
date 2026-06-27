/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.imagepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.data.Image

@Composable
fun ImageItem(image: Image, onShowImage: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Card(
            onClick = onShowImage,
        ) {
            Column(Modifier.padding(16.dp)) {
                ImageIcon(image)
                GeneralSpacer(2.dp)
                Text(
                    text = image.toReadableName(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
