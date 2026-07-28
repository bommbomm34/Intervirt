/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.model

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.are_you_sure_to_remove_device
import intervirt.ui.generated.resources.external_port_already_bound
import intervirt.ui.generated.resources.internal_port_already_exposed
import intervirt.ui.generated.resources.port_out_of_range
import io.github.bommbomm34.intervirt.components.device.settings.AddPortForwardingDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.components.dialogs.openAcceptDialog
import io.github.bommbomm34.intervirt.components.filepicker.ContainerFilePicker
import io.github.bommbomm34.intervirt.core.api.atomic.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.atomic.Holder
import io.github.bommbomm34.intervirt.core.api.atomic.ProjectHolder
import io.github.bommbomm34.intervirt.core.api.atomic.getValue
import io.github.bommbomm34.intervirt.core.api.isValidPort
import io.github.bommbomm34.intervirt.core.data.*
import io.github.bommbomm34.intervirt.core.util.ext.getLogger
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.openDialog
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
    envHolder: AppEnvHolder,
    private val appState: AppState,
    private val deviceManager: DeviceManager,
    private val guestManager: GuestManager,
    @InjectedParam val deviceId: DeviceId,
) : ViewModel() {
    var project by mutableStateOf(appState.project.value)
    val device get() = project.devices.first { it.id == deviceId }
    val appEnv by envHolder
    val computer: Device.Computer
        get() {
            val device = device
            check(device is Device.Computer) { "Expected computer, actual $device" }
            return device
        }
    var showPortForwardings: Boolean by mutableStateOf(false)
    private var containerFilePath: Path? by mutableStateOf(null)
    var ioClient: ContainerIOClient? by mutableStateOf(null)
    var info: AgentInfo? by mutableStateOf(null)
    private val logger = appEnv.getLogger(DeviceSettingsViewModel::class)
    private val isComputer get() = device is Device.Computer

    init {
        if (isComputer) {
            viewModelScope.launchDialogCatching(appState) {
                logger.debug { "Initializing IO client for $device" }
                ioClient = deviceManager.getIOClient(computer)
                logger.debug { "Retrieving AgentInfo for $device" }
                info = guestManager.getInfo()
            }
        }

        viewModelScope.launchDialogCatching(appState) {
            appState.project.collect { project = it }
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
        deviceManager.start(computer)
    }

    fun stop() = viewModelScope.launchDialogCatching(appState) {
        deviceManager.stop(computer)
    }

    fun changeIpv4(ipv4: String) = viewModelScope.launchDialogCatching(appState) {
        deviceManager.setIpv4(computer, ipv4)
    }

    fun changeIpv6(ipv6: String) = viewModelScope.launchDialogCatching(appState) {
        deviceManager.setIpv6(computer, ipv6)
    }

    fun changeName(name: String) = viewModelScope.launchDialogCatching(appState) {
        deviceManager.setName(device, name)
    }

    fun delete(onClose: () -> Unit) {
        appState.openAcceptDialog(Res.string.are_you_sure_to_remove_device, device.name) {
            viewModelScope.launchDialogCatching(appState) {
                deviceManager.removeDevice(device)
                close() // Accept dialog
                onClose() // Device settings
            }
        }
    }

    fun enableInternetAccess(enabled: Boolean) = viewModelScope.launchDialogCatching(appState) {
        deviceManager.setInternetEnabled(computer, enabled)
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
        deviceManager.addPortForwarding(computer, portForwarding)
    }

    fun removePortForwarding(portForwarding: PortForwarding) = viewModelScope.launchDialogCatching(appState) {
        deviceManager.removePortForwarding(portForwarding.externalPort, portForwarding.protocol)
    }

    suspend fun lintPortForwarding(portForwarding: PortForwarding): Either<Failure.PortForwardingValidationFailure, Unit> {
        val bindResult = portForwarding.externalPort.canPortBind()
        return when {
            !portForwarding.internalPort.isValidPort() ->
                Failure.PortForwardingValidationFailure(
                    portForwarding,
                    getString(Res.string.port_out_of_range, portForwarding.internalPort.toString()),
                ).left()

            !portForwarding.externalPort.isValidPort() ->
                Failure.PortForwardingValidationFailure(
                    portForwarding,
                    getString(Res.string.port_out_of_range, portForwarding.externalPort.toString())
                ).left()

            computer.portForwardings.any { it.internalPort == portForwarding.internalPort } ->
                Failure.PortForwardingValidationFailure(
                    portForwarding,
                    getString(
                        Res.string.internal_port_already_exposed,
                    ),
                ).left()

            project.devices.any { device ->
                if (device is Device.Computer) device.portForwardings.any {
                    it.externalPort == portForwarding.externalPort && it.protocol == portForwarding.protocol
                } else false
            } -> Failure.PortForwardingValidationFailure(
                portForwarding,
                getString(Res.string.external_port_already_bound),
            ).left()

            bindResult.isFailure -> Failure.PortForwardingValidationFailure(
                portForwarding,
                bindResult.exceptionOrNull()!!.message ?: "Unknown",
            ).left()

            else -> Unit.right()
        }
    }
}
