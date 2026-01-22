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
import io.github.bommbomm34.intervirt.api.AgentClient
import io.github.bommbomm34.intervirt.api.QemuClient
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.logs
import io.github.bommbomm34.intervirt.openDialog
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SyncButton() {
    val scope = rememberCoroutineScope()
    var syncing by remember { mutableStateOf(false) }
    var syncFailed by remember { mutableStateOf(false) }
    val qemuClient = koinInject<QemuClient>()
    val agentClient = koinInject<AgentClient>()
    if (qemuClient.isRunning()){
        IconButton(
            onClick = {
                scope.launch {
                    syncing = true
                    configuration.syncConfiguration(agentClient).collect {
                        syncFailed = it.result?.isFailure ?: false
                        logs.add(it.log())
                        if (syncFailed) {
                            openDialog(
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