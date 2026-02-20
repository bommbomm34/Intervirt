package io.github.bommbomm34.intervirt.gui.components.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.DialogState
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun Dialog(state: DialogState) {
    AlignedBox(Alignment.Center) {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colors.background.copy(blue = 0.05f),
        ) {
            Box(Modifier.padding(16.dp)) {
                state.compose()
            }
        }
    }
}

fun CoroutineScope.launchDialogCatching(
    appState: AppState,
    block: suspend CoroutineScope.() -> Unit
) {
    launch {
        appState.runDialogCatching {
            block()
        }
    }
}