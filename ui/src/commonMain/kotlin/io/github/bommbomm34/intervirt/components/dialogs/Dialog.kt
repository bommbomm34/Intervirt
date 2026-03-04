package io.github.bommbomm34.intervirt.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.DialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun Dialog(state: DialogState) {
    AlignedBox(Alignment.Center) {
        state.compose()
    }
}

fun CoroutineScope.launchDialogCatching(
    appState: AppState,
    block: suspend CoroutineScope.() -> Unit,
): Job = launch {
    appState.runDialogCatching {
        block()
    }
}