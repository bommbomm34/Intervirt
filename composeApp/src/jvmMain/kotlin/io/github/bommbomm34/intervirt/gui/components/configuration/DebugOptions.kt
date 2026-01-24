package io.github.bommbomm34.intervirt.gui.components.configuration

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.bommbomm34.intervirt.CURRENT_VERSION
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.api.QemuClient
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun DebugOptions() {
    val appState = koinInject<AppState>()
    val qemuClient = koinInject<QemuClient>()
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val logger = remember { KotlinLogging.logger {  } }
    Text("Debugging enabled")
    Text("Current version: $CURRENT_VERSION")
    Button(onClick = {
        appState.openDialog(
            importance = Importance.INFO,
            message = "Debug with: ./gradlew"
        )
    }) {
        Text("Debug Agent")
    }
    Row {
        var command by remember { mutableStateOf("") }
        OutlinedTextField(
            value = command,
            onValueChange = { command = it },
            placeholder = { Text("Command for QEMU Monitor") }
        )
        Button(
            onClick = {
                scope.launch {
                    if (!qemuClient.isRunning()) qemuClient.bootAlpine().getOrThrow()
                    qemuClient.monitorSend(command).collect {
                        logger.info { "Command result of $command: $it" }
                    }
                }

            }
        ){
            Text("Send")
        }
    }
}