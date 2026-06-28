/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import arrow.core.raise.Raise
import arrow.core.raise.recover
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.agent_timeout
import intervirt.ui.generated.resources.allStringResources
import intervirt.ui.generated.resources.command_execution_failure
import intervirt.ui.generated.resources.container_execution_failure
import intervirt.ui.generated.resources.download_failure
import intervirt.ui.generated.resources.illegal_agent_response
import intervirt.ui.generated.resources.illegal_argument
import intervirt.ui.generated.resources.illegal_state
import intervirt.ui.generated.resources.invalid_mail
import intervirt.ui.generated.resources.not_found
import intervirt.ui.generated.resources.not_supported_operation
import intervirt.ui.generated.resources.operation_already_performed
import intervirt.ui.generated.resources.os_failure
import intervirt.ui.generated.resources.qmp_failure
import intervirt.ui.generated.resources.serialization_failure
import intervirt.ui.generated.resources.undefined_failure
import intervirt.ui.generated.resources.unexpected_failure
import intervirt.ui.generated.resources.unknown_failure
import intervirt.ui.generated.resources.version_mismatch
import intervirt.ui.generated.resources.zip_extraction_failure
import io.github.bommbomm34.intervirt.components.dialogs.DefaultDialog
import io.github.bommbomm34.intervirt.core.CURRENT_VERSION
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.util.Atomic
import io.github.vinceglb.filekit.PlatformFile
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class AppState(project: Atomic<Project>) {
    val logs = mutableStateListOf<String>()
    var showLogs by mutableStateOf(false)
    var dialogStates = mutableStateListOf<DialogState>()
    var devicesViewZoom by mutableFloatStateOf(1f)
    var isCtrlPressed by mutableStateOf(false)
    var mousePosition by mutableStateOf(Offset.Zero)
    var currentFile: PlatformFile? by mutableStateOf(null)
    var currentScreen by mutableStateOf(Screen.HOME)
    var osWindowTitle: String? by mutableStateOf(null)
    var openComputerShell: ViewDevice.Computer? by mutableStateOf(null)
    val statefulProject = project.get().toViewProject()
    var windowState = WindowState(size = DpSize(1200.dp, 1000.dp))
    var drawingConnectionSource: ViewDevice? by mutableStateOf(null)
    var deviceSettingsVisible by mutableStateOf(false)
    var appEnvChangeKey by mutableIntStateOf(0)
    val images = mutableStateListOf<Image>()
}

fun AppState.openDialog(
    severity: Severity,
    message: String,
    title: String = message,
    copyMessage: String = message,
) = openDialog(title) {
    DefaultDialog(
        message = message,
        severity = severity,
        copyMessage = copyMessage,
        onClose = ::close,
    )
}

fun AppState.openDialog(
    title: String = "",
    width: Dp = 600.dp,
    height: Dp = 300.dp,
    customContent: @Composable DialogState.() -> Unit,
): DialogState {
    val state = DialogState(title, DpSize(width, height), customContent) { dialogStates.remove(it) }
    dialogStates += state
    return state
}

suspend fun AppState.showFailureDialog(failure: Failure) {
    openDialog(
        severity = Severity.ERROR,
        message = failure.getLocalizedMessage(),
        copyMessage = failure.message,
    )
}

suspend inline fun AppState.runDialogCatching(block: context(Raise<Failure>) () -> Unit) {
    recover(
        block = block,
        recover = { showFailureDialog(it) }
    )
}

private suspend fun Failure.getLocalizedMessage(): String {
    return when (this) {
        is Failure.AgentTimeout -> getString(Res.string.agent_timeout)
        is Failure.ContainerExecution -> getString(Res.string.container_execution_failure, message)
        is Failure.IllegalAgentResponse -> getString(Res.string.illegal_agent_response, message)
        is Failure.IllegalArgument -> getString(Res.string.illegal_argument, message)
        is Failure.NotFound -> getString(Res.string.not_found, message)
        is Failure.NotSupportedOperation -> getString(Res.string.not_supported_operation, message)
        is Failure.OS -> getString(Res.string.os_failure, message)
        is Failure.OperationAlreadyPerformed -> getString(Res.string.operation_already_performed, message)
        is Failure.Undefined -> getString(Res.string.undefined_failure, message)
        is Failure.Unknown -> getString(Res.string.unknown_failure)
        is Failure.CommandExecution -> getString(Res.string.command_execution_failure, message)
        is Failure.Download -> getString(Res.string.download_failure, message)
        is Failure.IllegalState -> getString(Res.string.illegal_state, message)
        is Failure.InvalidMail -> getString(Res.string.invalid_mail, message)
        is Failure.Qmp -> getString(Res.string.qmp_failure, message)
        is Failure.Serialization -> getString(Res.string.serialization_failure, message)
        is Failure.Unexpected -> getString(Res.string.unexpected_failure, message)
        is Failure.VersionMismatch -> getString(Res.string.version_mismatch, CURRENT_VERSION, other)
        is Failure.ZipExtraction -> getString(Res.string.zip_extraction_failure, message)
        is Failure.PortForwardingValidationFailure -> throw IllegalStateException("This failure shouldn't be exposed: $this")
    }
}
