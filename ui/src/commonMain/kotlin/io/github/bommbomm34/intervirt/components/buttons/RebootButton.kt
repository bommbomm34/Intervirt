package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.reboot
import intervirt.ui.generated.resources.rebooting
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun RebootButton(running: Boolean) {
    val appState = koinInject<AppState>()
    val guestManager = koinInject<GuestManager>()
    val scope = rememberCoroutineScope()
    val rebootText = stringResource(Res.string.reboot)
    val rebootingText = stringResource(Res.string.rebooting)
    var rebootButtonText by remember { mutableStateOf(rebootText) }
    Button(
        onClick = {
            scope.launch {
                rebootButtonText = rebootingText
                guestManager.reboot()
                    .onFailure {
                        appState.openDialog(
                            severity = Severity.ERROR,
                            message = it.localizedMessage,
                        )
                    }
                rebootButtonText = rebootText
            }
        },
        enabled = running,
    ) {
        Text(rebootButtonText)
    }
}