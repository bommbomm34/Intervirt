/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize

data class DialogState(
    val title: String = "",
    val size: DpSize,
    private val content: @Composable DialogState.() -> Unit,
    private val onClose: (DialogState) -> Unit,
) {
    @Composable
    fun compose() = content(this)

    fun close() = onClose(this)
}

enum class Severity {
    INFO, ERROR, WARNING
}