package io.github.bommbomm34.intervirt.components.dialogs

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.cancel
import intervirt.ui.generated.resources.close
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.FlowProgressView
import io.github.bommbomm34.intervirt.components.GeneralSpacer
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
    CenterColumn {
        FlowProgressView(flow, { job = it }, onMessage)
        GeneralSpacer()
        if (job != null) {
            Button(
                onClick = { job?.cancel() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Text(
                    text = stringResource(Res.string.cancel),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        } else {
            Button(onClick = onClose){
                Text(stringResource(Res.string.close))
            }
        }
    }
}