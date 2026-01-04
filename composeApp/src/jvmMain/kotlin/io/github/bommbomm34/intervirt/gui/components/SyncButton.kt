package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import compose.icons.TablerIcons
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.RefreshAlert
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.sync_guest
import intervirt.composeapp.generated.resources.syncing
import io.github.bommbomm34.intervirt.api.QEMUClient
import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.logs
import io.github.bommbomm34.intervirt.openDialog
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun SyncButton(){
    val scope = rememberCoroutineScope()
    var syncing by remember { mutableStateOf(false) }
    var syncFailed by remember { mutableStateOf(false) }
    AlignedBox(Alignment.TopEnd){
        IconButton(
            onClick = {
                scope.launch {
                    syncing = true
                    configuration.syncConfiguration().collect {
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
            enabled = QEMUClient.isRunning() && !syncing
        ){
            Icon(
                imageVector = if (syncFailed) TablerIcons.RefreshAlert else TablerIcons.Refresh,
                contentDescription = stringResource(if (syncing) Res.string.syncing else Res.string.sync_guest),
                tint = MaterialTheme.colors.onBackground
            )
        }
    }
}