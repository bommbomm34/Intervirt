package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.PlayButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SshServer(
    computer: ViewDevice.Computer
){
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val deviceManager = koinInject<DeviceManager>()
    var running by remember { mutableStateOf(false) }
    AlignedBox(Alignment.TopEnd){
        PlayButton(running){
            scope.launch {
                deviceManager.enableSshServer(computer, it)
            }
        }
    }
}

private suspend fun DeviceManager.enableSshServer(
    computer: ViewDevice.Computer,
    enabled: Boolean
): Result<Unit> {
    runCommand(
        computer = computer.device,
        command = "systemctl ${if (enabled) "start" else "stop"} ssh"
    )
        .firstOrNull { it.result?.isFailure ?: false }
        ?.let { return Result.failure(it.result!!.exceptionOrNull()!!) }
    return Result.success(Unit)
}