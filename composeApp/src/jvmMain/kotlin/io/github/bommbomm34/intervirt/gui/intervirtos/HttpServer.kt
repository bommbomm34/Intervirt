package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.enable_virtual_hosts
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.NamedCheckbox
import io.github.bommbomm34.intervirt.gui.components.buttons.PlayButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun HttpServer(
    computer: ViewDevice.Computer
) {
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val deviceManager = koinInject<DeviceManager>()
    var running by remember { mutableStateOf(false) }
    var enableVirtualHosts by remember { mutableStateOf(false) }
    AlignedBox(Alignment.TopEnd) {
        PlayButton(running) {
            scope.launch { deviceManager.enableHttpServer(computer, it) }
        }
        GeneralSpacer()
        NamedCheckbox(
            checked = enableVirtualHosts,
            onCheckedChange = { enableVirtualHosts = it },
            name = stringResource(Res.string.enable_virtual_hosts)
        )
        GeneralSpacer()
        AnimatedVisibility(enableVirtualHosts){
            // TODO: Virtual hosts
        }
    }
}

private suspend fun DeviceManager.enableHttpServer(
    computer: ViewDevice.Computer,
    enabled: Boolean
): Result<Unit> {
    runCommand(
        computer = computer.device,
        command = "systemctl ${if (enabled) "start" else "stop"} apache2"
    )
        .firstOrNull { it.result?.isFailure ?: false }
        ?.let { return Result.failure(it.result!!.exceptionOrNull()!!) }
    return Result.success(Unit)
}