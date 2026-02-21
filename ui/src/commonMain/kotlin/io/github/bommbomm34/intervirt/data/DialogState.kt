package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.Composable

data class DialogState(
    val title: String = "",
    private val content: @Composable DialogState.() -> Unit,
    private val onClose: (DialogState) -> Unit,
){
    @Composable
    fun compose() = content(this)

    fun close() = onClose(this)
}

enum class Severity {
    INFO, ERROR, WARNING
}