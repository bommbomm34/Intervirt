package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.browse
import intervirt.composeapp.generated.resources.failed_to_load_proxy
import intervirt.composeapp.generated.resources.url
import intervirt.composeapp.generated.resources.waiting_for_container_proxy
import io.github.bommbomm34.intervirt.HOMEPAGE_URL
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AlignedColumn
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.CenterRow
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.webview.ProxyWebView
import io.github.bommbomm34.intervirt.gui.components.webview.rememberWebViewState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Browser(
    computer: ViewDevice.Computer
){
    val deviceManager = koinInject<DeviceManager>()
    val state = rememberWebViewState(HOMEPAGE_URL)
    var url by remember { mutableStateOf("") }
    var proxyUrl: Result<String>? by remember { mutableStateOf(null) }
    LaunchedEffect(computer.id){
        proxyUrl = deviceManager.getProxy(computer.device)
    }
    CenterColumn {
        CenterRow {
            AlignedColumn(Alignment.CenterHorizontally){
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(Res.string.url)) }
                )
            }
            GeneralSpacer()
            AlignedColumn(Alignment.End){
                Button(
                    onClick = { state.url = url }
                ){
                    Text(stringResource(Res.string.browse))
                }
            }
        }
        GeneralSpacer()
        val proxy = proxyUrl
        if (proxy != null){
            proxy.fold(
                onSuccess = {
                    state.proxy = it
                    ProxyWebView(state)
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