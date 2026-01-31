package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.api.QemuClient
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ShutdownButton(){
    val scope = rememberCoroutineScope()
    val bootText = stringResource(Res.string.boot)
    val shutdownText = stringResource(Res.string.shutdown)
    val shuttingDownText = stringResource(Res.string.shutting_down)
    val bootingText = stringResource(Res.string.booting)
    val qemuClient = koinInject<QemuClient>()
    var powerButtonText by remember { mutableStateOf(bootText) }
    Button(
        onClick = {
            scope.launch {
                if (qemuClient.running) {
                    powerButtonText = shuttingDownText
                    qemuClient.shutdownAlpine()
                    powerButtonText = bootText
                } else {
                    powerButtonText = bootingText
                    qemuClient.bootAlpine()
                    powerButtonText = shutdownText
                }
            }
        }
    ) {
        Text(powerButtonText)
    }
}