package io.github.bommbomm34.intervirt.gui.components.configuration

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.CURRENT_VERSION
import io.github.bommbomm34.intervirt.api.QemuClient
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.data.stateful.AppState
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.rememberLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject

@Composable
fun DebugOptions() {
    val appState = koinInject<AppState>()
    val qemuClient = koinInject<QemuClient>()
    val scope = rememberCoroutineScope()
    val logger = rememberLogger()
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
    GeneralSpacer()
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
                    if (!qemuClient.running) qemuClient.bootAlpine().getOrThrow()
                    val res = qemuClient.qmpSend(command).getOrNull()?.let { Json.encodeToString(it) }
                    logger.debug { "Command result of $command: $res" }
                }

            }
        ){
            Text("Send")
        }
    }
    GeneralSpacer()
    Button(
        onClick = {
            scope.launch {
                qemuClient.addPortForwarding(
                    protocol = "tcp",
                    hostPort = 8999,
                    guestPort = 22
                ).onFailure { logger.error(it){ "Example port forwarding creation failed" } }
            }
        }
    ){
        Text("Add example port forwarding")
    }
    Button(
        onClick = {
            scope.launch {
                qemuClient.removePortForwarding(
                    protocol = "tcp",
                    hostPort = 8999
                ).onFailure { logger.error(it){ "Example port forwarding creation failed" } }
            }
        }
    ){
        Text("Remove example port forwarding")
    }
}