package io.github.bommbomm34.intervirt.gui.imagepicker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.onClick
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.data.Image
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageItem(image: Image, onShowImage: () -> Unit){
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.onClick(onClick = onShowImage)
    ) {
        ImageIcon(image)
        GeneralSpacer(2.dp)
        Text(image.toReadableName())
    }
}