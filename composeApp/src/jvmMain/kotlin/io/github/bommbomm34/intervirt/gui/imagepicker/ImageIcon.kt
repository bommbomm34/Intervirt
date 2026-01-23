package io.github.bommbomm34.intervirt.gui.imagepicker

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.debian
import io.github.bommbomm34.intervirt.data.Image
import io.github.bommbomm34.intervirt.api.Preferences
import org.jetbrains.compose.resources.Resource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject

@Composable
fun ImageIcon(image: Image) {
    val preferences = koinInject<Preferences>()
    androidx.compose.foundation.Image(
        painter = painterResource(image.icon),
        contentDescription = image.toReadableName(),
        modifier = Modifier.size(preferences.OS_ICON_SIZE)
    )
}