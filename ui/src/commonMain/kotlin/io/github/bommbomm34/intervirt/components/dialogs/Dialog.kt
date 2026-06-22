/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.DialogState
import io.github.bommbomm34.intervirt.data.runDialogCatching
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
    block: suspend context(Raise<Failure>) CoroutineScope.() -> Unit,
): Job = launch {
    appState.runDialogCatching {
        block()
    }
}
