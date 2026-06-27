/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.configuration

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import arrow.core.raise.recover
import io.github.bommbomm34.intervirt.CURRENT_VERSION
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.core.defaultJson
import io.github.bommbomm34.intervirt.core.error
import io.github.bommbomm34.intervirt.core.getOrNull
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import io.github.bommbomm34.intervirt.data.openDialog
import io.github.bommbomm34.intervirt.rememberLogger
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun DebugOptions() {
    val appState = koinInject<AppState>()
    val qemuClient = koinInject<QemuClient>()
    val scope = rememberCoroutineScope()
    val logger = rememberLogger("DebugOptions")
    Text("Debugging enabled")
    Text("Current version: $CURRENT_VERSION")
    Button(
        onClick = {
            appState.openDialog(
                severity = Severity.INFO,
                message = "Debug with: ./gradlew",
            )
        },
    ) {
        Text("Debug Agent")
    }
    GeneralSpacer()
    Row {
        var command by remember { mutableStateOf("") }
        OutlinedTextField(
            value = command,
            onValueChange = { command = it },
            placeholder = { Text("Command for QEMU Monitor") },
        )
        Button(
            onClick = {
                scope.launchDialogCatching(appState) {
                    if (!qemuClient.running) qemuClient.bootAlpine()
                    val res = getOrNull { qemuClient.qmpSend(command) }?.let { defaultJson.encodeToString(it) }
                    logger.debug { "Command result of $command: $res" }
                }

            },
        ) {
            Text("Send")
        }
    }
    GeneralSpacer()
    Button(
        onClick = {
            scope.launchDialogCatching(appState) {
                qemuClient.addPortForwarding(
                    protocol = "tcp",
                    externalPort = 8999,
                    internalPort = 22,
                )
            }
        },
    ) {
        Text("Add example port forwarding")
    }
    Button(
        onClick = {
            scope.launchDialogCatching(appState) {
                qemuClient.removePortForwarding(
                    protocol = "tcp",
                    externalPort = 8999,
                )
            }
        },
    ) {
        Text("Remove example port forwarding")
    }
    Button(
        onClick = {
            throw IllegalStateException("Someone has thrown an exception!")
        }
    ){
        Text("Throw exception")
    }
}
