package io.github.bommbomm34.intervirt.gui.imagepicker

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.Image
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun ImageIcon(image: Image) {
    val appEnv = koinInject<AppEnv>()
    androidx.compose.foundation.Image(
        painter = painterResource(image.icon),
        contentDescription = image.toReadableName(),
        modifier = Modifier.size(appEnv.osIconSize.dp)
    )
}