package io.github.bommbomm34.intervirt.data

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import io.github.bommbomm34.intervirt.components.dialogs.DefaultDialog
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.runSuspendingCatching
import io.github.vinceglb.filekit.PlatformFile

class AppState(configuration: IntervirtConfiguration) {
    val logs = mutableStateListOf<String>()
    var showLogs by mutableStateOf(false)
    var dialogStates = mutableStateListOf<DialogState>()
    var devicesViewZoom by mutableStateOf(1f)
    var isCtrlPressed by mutableStateOf(false)
    var mousePosition by mutableStateOf(Offset.Zero)
    val currentFile: PlatformFile? by mutableStateOf(null)
    var currentScreenIndex by mutableStateOf(1)
    var osWindowTitle: String? by mutableStateOf(null)
    var openComputerShell: ViewDevice.Computer? by mutableStateOf(null)
    val statefulConf = ViewConfiguration(configuration)
    var windowState = WindowState(size = DpSize(1200.dp, 1000.dp))
    var drawingConnectionSource: ViewDevice? by mutableStateOf(null)
    var deviceSettingsVisible by mutableStateOf(false)
    var appEnvChangeKey by mutableStateOf(0)
    val images = mutableStateListOf<Image>()

    fun openDialog(
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

    fun openDialog(
        title: String = "",
        width: Dp = 600.dp,
        height: Dp = 300.dp,
        customContent: @Composable DialogState.() -> Unit,
    ): DialogState {
        val state = DialogState(title, DpSize(width, height), customContent) { dialogStates.remove(it) }
        dialogStates.add(state)
        return state
    }

    /**
     * Open error dialog if result is failed, otherwise call `onSuccess`
     */
    suspend inline fun <T> runDialogCatching(block: suspend () -> T): Result<T> =
        runSuspendingCatching(block).onFailure {
            showExceptionDialog(it)
        }

    suspend fun showExceptionDialog(throwable: Throwable) {
        throwable.printStackTrace()
        openDialog(
            severity = Severity.ERROR,
            message = throwable.parseException().message,
            copyMessage = throwable.stackTraceToString(),
        )
    }
}