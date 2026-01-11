package io.github.bommbomm34.intervirt.gui.imagepicker

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.data.Image
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ImageIcon(image: Image) = Icon(
    imageVector = vectorResource(image.icon),
    contentDescription = image.name
)