package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.api.impl.getTotalCommandStatus
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.exceptions.ContainerExecutionException
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.PlayButton
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SshServer(
    computer: ViewDevice.Computer
){
    val scope = rememberCoroutineScope()
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
    val total = runCommand(
        computer = computer.device,
        commands = listOf("systemctl", if (enabled) "start" else "stop", "ssh")
    ).getTotalCommandStatus()

    return if (total.statusCode!! == 0) Result.success(Unit)
    else Result.failure(ContainerExecutionException(total.message!!))
}