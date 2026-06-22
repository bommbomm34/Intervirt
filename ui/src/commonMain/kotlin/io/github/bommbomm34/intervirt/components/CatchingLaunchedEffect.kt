/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import arrow.core.raise.Raise
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.runDialogCatching
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

@Composable
fun CatchingLaunchedEffect(
    vararg keys: Any?,
    block: suspend context(Raise<Failure>) CoroutineScope.() -> Unit,
) {
    val appState = koinInject<AppState>()
    LaunchedEffect(*keys) {
        appState.runDialogCatching { block() }
    }
}
