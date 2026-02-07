package io.github.bommbomm34.intervirt.data.stateful

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.DialogState
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.runSuspendingCatching
import io.github.vinceglb.filekit.PlatformFile
import kotlin.fold

class AppState {
    val logs = mutableStateListOf<String>()
    var showLogs by mutableStateOf(false)
    var dialogState: DialogState by mutableStateOf(DialogState.Default)
    var devicesViewZoom by mutableStateOf(1f)
    var isCtrlPressed by mutableStateOf(false)
    var mousePosition by mutableStateOf(Offset.Zero)
    val currentFile: PlatformFile? by mutableStateOf(null)
    var currentScreenIndex by mutableStateOf(0)
    var osWindowTitle: String? by mutableStateOf(null)
    var openComputerShell: ViewDevice.Computer? by mutableStateOf(null)
    val statefulConf = ViewConfiguration(configuration)
    var windowState = WindowState(size = DpSize(1200.dp, 1000.dp))

    fun closeDialog() {
        dialogState = when (dialogState) {
            is DialogState.Custom -> (dialogState as DialogState.Custom).copy(visible = false)
            is DialogState.Regular -> (dialogState as DialogState.Regular).copy(visible = false)
        }
    }

    fun openDialog(
        importance: Importance,
        message: String
    ) {
        dialogState = DialogState.Regular(
            importance = importance,
            message = message,
            visible = true
        )
    }

    fun openDialog(customContent: @Composable () -> Unit) {
        dialogState = DialogState.Custom(customContent, true)
    }

    /**
     * Open error dialog if result is failed, otherwise call `onSuccess`
     */
    suspend inline fun <T> runDialogCatching(block: suspend () -> T): Result<T> = runSuspendingCatching(block).onFailure {
        openDialog(
            importance = Importance.ERROR,
            message = it.localizedMessage
        )
    }
}