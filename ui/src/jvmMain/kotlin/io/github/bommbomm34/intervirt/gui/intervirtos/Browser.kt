package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.HOMEPAGE_URL
import io.github.bommbomm34.intervirt.core.api.ContainerClientBundle
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.ProxyManager
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.gui.components.*
import io.github.bommbomm34.intervirt.rememberClient
import io.github.bommbomm34.intervirt.rememberProxyManager
import io.github.bommbomm34.intervirt.webview.Proxy
import io.github.bommbomm34.intervirt.webview.WebView
import io.github.bommbomm34.intervirt.webview.rememberWebViewNavigator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Browser(
    bundle: ContainerClientBundle
) {
    val appEnv = koinInject<AppEnv>()
    val browser = rememberProxyManager(appEnv, bundle)
    val deviceManager = koinInject <DeviceManager>()
    var url by remember { mutableStateOf("") } // URL in the search bar
    var currentUrl by remember { mutableStateOf(HOMEPAGE_URL) } // The URL which is loaded actually
    var proxyUrl: Result<Address>? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        proxyUrl = browser.getProxyUrl(deviceManager)
    }
    CenterColumn {
        CenterRow {
            AlignedColumn(Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(Res.string.url)) }
                )
            }
            GeneralSpacer()
            AlignedColumn(Alignment.End) {
                Button(
                    onClick = { currentUrl = url }
                ) {
                    Text(stringResource(Res.string.browse))
                }
            }
        }
        GeneralSpacer()
        val res = proxyUrl
        if (res != null) {
            res.fold(
                onSuccess = {
                    WebView(
                        url = currentUrl,
                        navigator = rememberWebViewNavigator(),
                        modifier = Modifier.fillMaxSize(),
                        proxy = Proxy(it.host, it.port)
                    )
                },
                onFailure = {
                    Text(
                        text = stringResource(Res.string.failed_to_load_proxy, it.localizedMessage),
                        color = Color.Red
                    )
                }
            )
        } else Text(stringResource(Res.string.waiting_for_container_proxy))
    }
}