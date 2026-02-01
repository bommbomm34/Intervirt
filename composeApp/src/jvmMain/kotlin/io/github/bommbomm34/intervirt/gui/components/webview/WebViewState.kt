package io.github.bommbomm34.intervirt.gui.components.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient

class WebViewState(initialUrl: String, val client: HttpClient) {
    var url by mutableStateOf(initialUrl)
    var proxy: String? by mutableStateOf(null)
}

@Composable
fun rememberWebViewState(url: String, client: HttpClient) = remember { WebViewState(url, client) }