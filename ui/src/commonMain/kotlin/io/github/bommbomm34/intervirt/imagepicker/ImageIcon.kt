package io.github.bommbomm34.intervirt.imagepicker

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Image
import io.github.bommbomm34.intervirt.rememberLogger
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ImageIcon(image: Image) {
    val logger = rememberLogger("ImageIcon")
    val appState = koinInject<AppState>()
    val appEnv = koinInject<AppEnv>()
    val scope = rememberCoroutineScope()
    AsyncImage(
        model = image.icon,
        onError = {
            scope.launch {
                System.err.println("An error occurred during image loading of $image")
                appState.showExceptionDialog(it.result.throwable)
            }
        },
        contentDescription = image.toReadableName(),
        modifier = Modifier.size(appEnv.OS_ICON_SIZE.dp),
    )
}