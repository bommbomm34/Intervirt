package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import intervirt.composeapp.generated.resources.*
import io.github.bommbomm34.intervirt.HOMEPAGE_URL
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.Address
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Browser(
    computer: ViewDevice.Computer
) {
    val deviceManager = koinInject<DeviceManager>()
    var url by remember { mutableStateOf("") } // URL in the search bar
    var currentUrl by remember { mutableStateOf(HOMEPAGE_URL) } // The URL which is loaded actually
    var proxyUrl: Result<Address>? by remember { mutableStateOf(null) }
    LaunchedEffect(computer.id) {
        proxyUrl = deviceManager.getProxyUrl(computer.device)
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
                    ProxyWebView(
                        url = currentUrl,
                        proxyAddress = it
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