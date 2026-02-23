package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.sync_guest
import intervirt.ui.generated.resources.syncing
import io.github.bommbomm34.intervirt.components.dialogs.ProgressDialog
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.data.AppState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SyncButton(running: Boolean) {
    val appState = koinInject<AppState>()
    val appEnv = koinInject<AppEnv>()
    val guestManager = koinInject<GuestManager>()
    val configuration = koinInject<IntervirtConfiguration>()
    val scope = rememberCoroutineScope()
    var syncing by remember { mutableStateOf(false) }
    var syncFailed by remember { mutableStateOf(false) }
    if (running || appEnv.VIRTUAL_AGENT_MODE) {
        IconButton(
            onClick = {
                scope.launch {
                    syncing = true
                    appState.openDialog {
                        ProgressDialog(
                            flow = configuration.syncConfiguration(guestManager),
                            onMessage = {
                                if (it is ResultProgress.Result) syncing = false
                            },
                            onClose = ::close,
                        )
                    }
                }
            },
            enabled = !syncing,
        ) {
            Icon(
                imageVector = if (syncFailed) Icons.Default.Refresh else Icons.Default.Autorenew,
                contentDescription = stringResource(if (syncing) Res.string.syncing else Res.string.sync_guest),
            )
        }
    }
}