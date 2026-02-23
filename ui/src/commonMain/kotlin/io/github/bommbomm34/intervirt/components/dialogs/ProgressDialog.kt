package io.github.bommbomm34.intervirt.components.dialogs

import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.FlowProgressView
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> ProgressDialog(
    flow: Flow<ResultProgress<T>>,
    onMessage: ((ResultProgress<T>) -> Unit)? = null,
    onClose: () -> Unit,
) {
    CenterColumn {
        FlowProgressView(flow, { if (it == null) onClose() }, onMessage)
    }
}