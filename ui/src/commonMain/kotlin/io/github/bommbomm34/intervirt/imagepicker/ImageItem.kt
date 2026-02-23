package io.github.bommbomm34.intervirt.imagepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.data.Image

@Composable
fun ImageItem(image: Image, onShowImage: () -> Unit) {
    Card(
        onClick = onShowImage,
    ) {
        Column(Modifier.padding(16.dp)){
            ImageIcon(image)
            GeneralSpacer(2.dp)
            Text(image.toReadableName())
        }
    }
}