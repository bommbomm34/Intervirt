package io.github.bommbomm34.intervirt.components.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.DialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun Dialog(state: DialogState) {
    AlignedBox(Alignment.Center) {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.background.copy(blue = 0.05f),
        ) {
            Box(Modifier.padding(16.dp)) {
                state.compose()
            }
        }
    }
}

fun CoroutineScope.launchDialogCatching(
    appState: AppState,
    block: suspend CoroutineScope.() -> Unit,
) {
    launch {
        appState.runDialogCatching {
            block()
        }
    }
}