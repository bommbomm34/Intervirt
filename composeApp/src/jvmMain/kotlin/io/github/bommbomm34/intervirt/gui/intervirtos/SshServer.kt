package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.api.Executor
import io.github.bommbomm34.intervirt.data.getTotalCommandStatus
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
    val executor = koinInject<Executor>()
    var running by remember { mutableStateOf(false) }
    AlignedBox(Alignment.TopEnd){
        PlayButton(running){
            scope.launch {
                executor.enableSshServer(computer, it)
            }
        }
    }
}

private suspend fun Executor.enableSshServer(
    computer: ViewDevice.Computer,
    enabled: Boolean
): Result<Unit> {
    TODO("Not yet implemented")
//    val total = runCommandOnGuest(
//        computer = computer.device,
//        commands = listOf("systemctl", if (enabled) "start" else "stop", "ssh")
//    ).getOrElse { return Result.failure(it) }.getTotalCommandStatus()
//
//    return if (total.statusCode!! == 0) Result.success(Unit)
//    else Result.failure(ContainerExecutionException(total.message!!))
}