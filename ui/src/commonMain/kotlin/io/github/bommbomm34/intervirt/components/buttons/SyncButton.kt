package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import compose.icons.TablerIcons
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.RefreshAlert
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.sync_guest
import intervirt.ui.generated.resources.syncing
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SyncButton(running: Boolean) {
    val appState = koinInject<AppState>()
    val guestManager = koinInject<GuestManager>()
    val configuration = koinInject<IntervirtConfiguration>()
    val scope = rememberCoroutineScope()
    var syncing by remember { mutableStateOf(false) }
    var syncFailed by remember { mutableStateOf(false) }
    if (running) {
        IconButton(
            onClick = {
                scope.launch {
                    syncing = true
                    configuration.syncConfiguration(guestManager).collect {
                        syncFailed = it is ResultProgress.Result && it.result.isFailure
                        appState.logs.add(it.log())
                        if (syncFailed) {
                            appState.openDialog(
                                severity = Severity.ERROR,
                                message = it.log(),
                            )
                            coroutineContext.cancel()
                        }
                    }
                    syncing = false
                }
            },
            enabled = !syncing,
        ) {
            Icon(
                imageVector = if (syncFailed) TablerIcons.RefreshAlert else TablerIcons.Refresh,
                contentDescription = stringResource(if (syncing) Res.string.syncing else Res.string.sync_guest),
            )
        }
    }
}