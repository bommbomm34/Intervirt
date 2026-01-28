package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.browse
import intervirt.composeapp.generated.resources.url
import io.github.bommbomm34.intervirt.HOMEPAGE_URL
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import io.github.bommbomm34.intervirt.gui.components.AlignedColumn
import io.github.bommbomm34.intervirt.gui.components.CenterColumn
import io.github.bommbomm34.intervirt.gui.components.CenterRow
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.kdroidfilter.webview.web.WebView
import io.github.kdroidfilter.webview.web.rememberWebViewNavigator
import io.github.kdroidfilter.webview.web.rememberWebViewState
import org.jetbrains.compose.resources.stringResource

@Composable
fun Browser(
    computer: ViewDevice.Computer
){
    var url by remember { mutableStateOf("") }
    val navigator = rememberWebViewNavigator()
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
                    onClick = { navigator.loadUrl(url) }
                ){
                    Text(stringResource(Res.string.browse))
                }
            }
        }
        GeneralSpacer()
        WebView(
            state = rememberWebViewState(HOMEPAGE_URL),
            navigator = navigator,
            modifier = Modifier.fillMaxSize()
        )
    }
}