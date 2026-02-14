package io.github.bommbomm34.intervirt.gui.intervirtos.http

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import io.github.bommbomm34.intervirt.core.api.intervirtos.HttpServerManager
import io.github.bommbomm34.intervirt.core.data.VirtualHost
import io.github.bommbomm34.intervirt.data.AppState
import org.koin.compose.koinInject

@Composable
fun VirtualHostsManager(httpServer: HttpServerManager) {
    val virtualHosts = remember { mutableStateListOf<VirtualHost>() }
    val appState = koinInject<AppState>()
    LaunchedEffect(virtualHosts){
        appState.runDialogCatching {
            httpServer.loadHttpConf(VirtualHost.generateConfiguration(virtualHosts))
        }
    }
    AddVirtualHostView { virtualHosts.add(it) }
    VirtualHostsTable(virtualHosts){ virtualHosts.remove(it) }
}