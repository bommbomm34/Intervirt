package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.reboot
import intervirt.composeapp.generated.resources.rebooting
import io.github.bommbomm34.intervirt.api.AgentClient
import io.github.bommbomm34.intervirt.api.QEMUClient
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.openDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun RebootButton(){
    val agentClient = koinInject<AgentClient>()
    val qemuClient = koinInject<QEMUClient>()
    val scope = rememberCoroutineScope()
    val rebootText = stringResource(Res.string.reboot)
    val rebootingText = stringResource(Res.string.rebooting)
    var rebootButtonText by remember { mutableStateOf(rebootText) }
    Button(
        onClick = {
            scope.launch {
                rebootButtonText = rebootingText
                agentClient.reboot()
                    .onFailure {
                        openDialog(
                            importance = Importance.ERROR,
                            message = it.localizedMessage
                        )
                    }
                rebootButtonText = rebootText
            }
        },
        enabled = qemuClient.isRunning()
    ){
        Text(rebootButtonText)
    }
}