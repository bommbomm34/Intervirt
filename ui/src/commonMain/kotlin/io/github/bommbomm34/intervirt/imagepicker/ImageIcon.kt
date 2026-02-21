package io.github.bommbomm34.intervirt.imagepicker

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.Image
import org.koin.compose.koinInject

@Composable
fun ImageIcon(image: Image) {
    val appEnv = koinInject<AppEnv>()
    AsyncImage(
        model = image.icon,
        contentDescription = image.toReadableName(),
        modifier = Modifier.size(appEnv.OS_ICON_SIZE.dp),
    )
}