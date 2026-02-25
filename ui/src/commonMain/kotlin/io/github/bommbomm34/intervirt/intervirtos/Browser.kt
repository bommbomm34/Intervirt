package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.browse
import intervirt.ui.generated.resources.url
import intervirt.ui.generated.resources.waiting_for_container_proxy
import io.github.bommbomm34.intervirt.HOMEPAGE_URL
import io.github.bommbomm34.intervirt.components.*
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.rememberProxyManager
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Browser(
    osClient: IntervirtOSClient,
) {
    val appEnv = koinInject<AppEnv>()
    val deviceManager = koinInject<DeviceManager>()
    val browser = rememberProxyManager(appEnv, deviceManager, osClient)
    var url by remember { mutableStateOf("") } // URL in the search bar
    var currentUrl by remember { mutableStateOf(HOMEPAGE_URL) } // The URL which is loaded actually
    var proxyUrl: Address? by remember { mutableStateOf(null) }
    CatchingLaunchedEffect {
        proxyUrl = browser.getProxyUrl().getOrThrow()
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
                    onClick = { currentUrl = url },
                ) {
                    Text(stringResource(Res.string.browse))
                }
            }
        }
        GeneralSpacer()
        val url = proxyUrl
        if (url != null) {
            // TODO: Wait for PR #23 of kdroidFilter/ComposeNativeWebview to get merged
//            WebView(
//                url = currentUrl,
//                navigator = rememberWebViewNavigator(),
//                modifier = Modifier.fillMaxSize(),
//                proxy = Proxy(url.host, url.port),
//            )
        } else Text(stringResource(Res.string.waiting_for_container_proxy))
    }
}