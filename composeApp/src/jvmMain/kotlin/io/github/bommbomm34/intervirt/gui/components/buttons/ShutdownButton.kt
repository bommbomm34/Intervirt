package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.boot
import intervirt.composeapp.generated.resources.booting
import intervirt.composeapp.generated.resources.shutdown
import intervirt.composeapp.generated.resources.shutting_down
import io.github.bommbomm34.intervirt.api.QemuClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun ShutdownButton(){
    val scope = rememberCoroutineScope { Dispatchers.IO }
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