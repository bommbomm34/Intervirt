package io.github.bommbomm34.intervirt.components.dialogs

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.cancel
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.FlowProgressView
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.buttons.CloseButton
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> ProgressDialog(
    flow: Flow<ResultProgress<T>>,
    onMessage: ((ResultProgress<T>) -> Unit)? = null,
    onClose: () -> Unit,
) {
    var job: Job? by remember { mutableStateOf(null) }
    AlignedBox(Alignment.TopStart) {
        CloseButton(onClose)
    }
    CenterColumn {
        FlowProgressView(flow, { job = it }, onMessage)
        GeneralSpacer()
        job?.let {
            Button(
                onClick = {
                    it.cancel()
                    onClose()
                },
            ) {
                Text(stringResource(Res.string.cancel))
            }
        }
    }
}