package io.github.bommbomm34.intervirt.data

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

sealed class DialogState(
    open val visible: Boolean
) {
    companion object {
        val Default = Regular(
            importance = Importance.INFO,
            message = "",
            visible = false
        )
    }

    data class Regular(
        val importance: Importance,
        val message: String,
        override val visible: Boolean
    ) : DialogState(visible)

    data class Custom(
        val customContent: @Composable () -> Unit,
        override val visible: Boolean
    ) : DialogState(visible)
}

enum class Importance {
    INFO, ERROR, WARNING
}