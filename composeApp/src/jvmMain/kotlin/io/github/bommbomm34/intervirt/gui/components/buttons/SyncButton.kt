package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import compose.icons.TablerIcons
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.RefreshAlert
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.sync_guest
import intervirt.composeapp.generated.resources.syncing
import io.github.bommbomm34.intervirt.api.GuestManager
import io.github.bommbomm34.intervirt.api.QemuClient
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.data.stateful.AppState
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SyncButton(running: Boolean) {
    val appState = koinInject<AppState>()
    val scope = rememberCoroutineScope()
    var syncing by remember { mutableStateOf(false) }
    var syncFailed by remember { mutableStateOf(false) }
    val guestManager = koinInject<GuestManager>()
    if (running){
        IconButton(
            onClick = {
                scope.launch {
                    syncing = true
                    configuration.syncConfiguration(guestManager).collect {
                        syncFailed = it is ResultProgress.Result && it.result.isFailure
                        appState.logs.add(it.log())
                        if (syncFailed) {
                            appState.openDialog(
                                importance = Importance.ERROR,
                                message = it.log()
                            )
                            coroutineContext.cancel()
                        }
                    }
                    syncing = false
                }
            },
            enabled = !syncing
        ) {
            Icon(
                imageVector = if (syncFailed) TablerIcons.RefreshAlert else TablerIcons.Refresh,
                contentDescription = stringResource(if (syncing) Res.string.syncing else Res.string.sync_guest),
                tint = MaterialTheme.colors.onBackground
            )
        }
    }
}