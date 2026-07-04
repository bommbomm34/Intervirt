/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.browse
import intervirt.ui.generated.resources.url
import intervirt.ui.generated.resources.waiting_for_container_proxy
import io.github.bommbomm34.intervirt.HOMEPAGE_URL
import io.github.bommbomm34.intervirt.components.*
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.util.ext.rememberProxyManager
import io.github.kdroidfilter.webview.setting.ProxyConfig
import io.github.kdroidfilter.webview.web.WebView
import io.github.kdroidfilter.webview.web.rememberWebViewNavigator
import io.github.kdroidfilter.webview.web.rememberWebViewState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Browser(
    osClient: IntervirtOSClient,
) {
    val deviceManager = koinInject<DeviceManager>()
    val browser = rememberProxyManager(currentAppEnv, deviceManager, osClient)
    var url by remember { mutableStateOf("") } // URL in the search bar
    var proxyUrl: Address? by remember { mutableStateOf(null) }
    val navigator = rememberWebViewNavigator()
    CatchingLaunchedEffect(browser) {
        proxyUrl = browser.getProxyUrl()
    }
    CenterColumn {
        CenterRow {
            AlignedColumn(Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(Res.string.url)) },
                )
            }
            GeneralSpacer()
            AlignedColumn(Alignment.End) {
                Button(
                    onClick = { navigator.loadUrl(url) },
                ) {
                    Text(stringResource(Res.string.browse))
                }
            }
        }
        GeneralSpacer()
        val url = proxyUrl
        if (url != null) {
            val state = rememberWebViewState(HOMEPAGE_URL) {
                desktopWebSettings.proxyConfig = ProxyConfig.Socks5(url.host, url.port)
            }
            WebView(
                state = state,
                navigator = navigator,
                modifier = Modifier.fillMaxSize(),
            )
        } else Text(stringResource(Res.string.waiting_for_container_proxy))
    }
}
