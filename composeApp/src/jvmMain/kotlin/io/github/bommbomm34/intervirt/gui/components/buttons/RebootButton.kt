package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.reboot
import intervirt.composeapp.generated.resources.rebooting
import io.github.bommbomm34.intervirt.api.GuestManager
import io.github.bommbomm34.intervirt.api.QemuClient
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.data.stateful.AppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun RebootButton(){
    val appState = koinInject<AppState>()
    val guestManager = koinInject<GuestManager>()
    val qemuClient = koinInject<QemuClient>()
    val scope = rememberCoroutineScope { Dispatchers.IO }
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
                            importance = Importance.ERROR,
                            message = it.localizedMessage
                        )
                    }
                rebootButtonText = rebootText
            }
        },
        enabled = qemuClient.running
    ){
        Text(rebootButtonText)
    }
}