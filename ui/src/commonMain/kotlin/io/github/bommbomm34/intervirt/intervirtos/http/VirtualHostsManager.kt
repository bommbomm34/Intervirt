/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos.http

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.core.api.intervirtos.HttpServerManager
import io.github.bommbomm34.intervirt.core.data.VirtualHost

@Composable
fun VirtualHostsManager(httpServer: HttpServerManager) {
    val virtualHosts = remember { mutableStateListOf<VirtualHost>() }
    CatchingLaunchedEffect(httpServer, virtualHosts) {
        httpServer.loadHttpConf(VirtualHost.generateConfiguration(virtualHosts))
    }
    AddVirtualHostView { virtualHosts.add(it) }
    VirtualHostsTable(virtualHosts) { virtualHosts.remove(it) }
}
