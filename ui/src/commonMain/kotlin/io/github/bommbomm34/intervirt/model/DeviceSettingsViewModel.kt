/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.external_port_already_bound
import intervirt.ui.generated.resources.internal_port_already_exposed
import io.github.bommbomm34.intervirt.components.device.settings.AddPortForwardingDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.components.filepicker.ContainerFilePicker
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.data.PortForwarding
import io.github.bommbomm34.intervirt.core.util.Atomic
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.ViewDevice
import io.github.bommbomm34.intervirt.util.ext.canPortBind

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.name

@KoinViewModel
class DeviceSettingsViewModel(
    appEnv: AppEnv,
    project: Atomic<Project>,
    private val appState: AppState,
    private val deviceManager: DeviceManager,
    @InjectedParam val device: ViewDevice,
) : ViewModel() {
    val computer: ViewDevice.Computer by lazy {
        check(device is ViewDevice.Computer) { "Expected computer, actual $device" }
        device
    }
    var showPortForwardings: Boolean by mutableStateOf(false)
    private var containerFilePath: Path? by mutableStateOf(null)
    var ioClient: ContainerIOClient? by mutableStateOf(null)
    private val logger = appEnv.getLogger(DeviceSettingsViewModel::class)
    private val isComputer get() = device is ViewDevice.Computer
    var project by project

    init {
        if (isComputer) {
            viewModelScope.launchDialogCatching(appState) {
                ioClient = deviceManager.getIOClient(computer.device).getOrThrow()
            }
        }
    }

    fun download() {
        logger.debug { "Downloading file from ${device.id}" }
        appState.openDialog(width = 1000.dp, height = 800.dp) {
            ContainerFilePicker(
                ioClient!!,
            ) { path ->
                close()
                path?.let {
                    containerFilePath = it
                    val fullFileName = path.name
                    viewModelScope.launch {
                        val file = FileKit.openFileSaver(
                            suggestedName = fullFileName.substringBefore("."),
                            extension = fullFileName.substringAfterLast("."),
                        )
                        file?.let {
                            viewModelScope.launchDialogCatching(appState) {
                                containerFilePath!!.copyTo(file.file.toPath(), overwrite = true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun upload() {
        logger.debug { "Uploading file to ${device.id}" }
        viewModelScope.launch {
            val file = FileKit.openFilePicker()
            file?.let { _ ->
                appState.openDialog(width = 1000.dp, height = 800.dp) {
                    val scope = rememberCoroutineScope()
                    ContainerFilePicker(
                        ioClient!!,
                        file.name,
                    ) { path ->
                        close()
                        path?.let { _ ->
                            scope.launchDialogCatching(appState) {
                                file.file.toPath().copyTo(path, true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun openShell() {
        appState.openComputerShell = computer
    }

    fun togglePortForwardings() {
        showPortForwardings = !showPortForwardings
    }

    fun start() = viewModelScope.launchDialogCatching(appState) {
        deviceManager.start(computer.device).getOrThrow()
        computer.running = true
    }

    fun stop() = viewModelScope.launchDialogCatching(appState) {
        deviceManager.stop(computer.device).getOrThrow()
        computer.running = false
    }

    fun changeIpv4(ipv4: String) = viewModelScope.launchDialogCatching(appState) {
        computer.ipv4 = ipv4
        deviceManager.setIpv4(computer.device, ipv4).getOrThrow()
    }

    fun changeIpv6(ipv6: String) = viewModelScope.launchDialogCatching(appState) {
        computer.ipv6 = ipv6
        deviceManager.setIpv6(computer.device, ipv6).getOrThrow()
    }

    fun enableInternetAccess(enabled: Boolean) = viewModelScope.launchDialogCatching(appState) {
        computer.internetEnabled = enabled
        deviceManager.setInternetEnabled(computer.device, enabled).getOrThrow()
    }

    fun openAddPortForwarding() {
        appState.openDialog(width = 800.dp) {
            AddPortForwardingDialog(
                onAdd = ::addPortForwarding,
                onLint = ::lintPortForwarding,
                onCancel = ::close,
            )
        }
    }

    fun addPortForwarding(portForwarding: PortForwarding) = viewModelScope.launchDialogCatching(appState) {
        computer.portForwardings.add(portForwarding)
        deviceManager.addPortForwarding(computer.device, portForwarding).getOrThrow()
    }

    fun removePortForwarding(portForwarding: PortForwarding) = viewModelScope.launchDialogCatching(appState) {
        computer.portForwardings.remove(portForwarding)
        deviceManager.removePortForwarding(portForwarding.externalPort, portForwarding.protocol)
            .getOrThrow()
    }

    suspend fun lintPortForwarding(portForwarding: PortForwarding): Result<Unit> {
        val bindResult = portForwarding.externalPort.canPortBind()
        return when {
            computer.portForwardings.any { it.internalPort == portForwarding.internalPort } -> Result.failure(
                IllegalArgumentException(
                    getString(
                        Res.string.internal_port_already_exposed,
                    ),
                ),
            )

            project.devices.any { device ->
                if (device is Device.Computer) device.portForwardings.any {
                    it.externalPort == portForwarding.externalPort && it.protocol == portForwarding.protocol
                } else false
            } -> Result.failure(
                IllegalArgumentException(getString(Res.string.external_port_already_bound)),
            )

            bindResult.isFailure -> Result.failure(bindResult.exceptionOrNull()!!)
            else -> Result.success(Unit)
        }
    }
}
