package io.github.bommbomm34.intervirt.gui.components.webview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.parkwoocheol.composewebview.ComposeWebView
import com.parkwoocheol.composewebview.PlatformWebResourceResponse
import com.parkwoocheol.composewebview.client.rememberWebViewClient
import com.parkwoocheol.composewebview.client.shouldInterceptRequest
import com.parkwoocheol.composewebview.rememberSaveableWebViewState
import com.parkwoocheol.composewebview.rememberWebViewController
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.util.toMap
import kotlinx.coroutines.runBlocking

@Composable
fun ProxyWebView(state: WebViewState) {
    val webviewState = rememberSaveableWebViewState(state.url)
    val controller = rememberWebViewController()
    val client = rememberWebViewClient {
        // Delegates requests to
        shouldInterceptRequest { _, request ->
            if (request == null) return@shouldInterceptRequest null
            runBlocking {
                val res = state.client.request {
                    url(request.url)
                    request.headers.forEach { (key, value) -> header(key, value) }
                    method = when (request.method){
                        "GET" -> HttpMethod.Get
                        "POST" -> HttpMethod.Post
                        "PUT" -> HttpMethod.Put
                        "HEAD" -> HttpMethod.Head
                        "DELETE" -> HttpMethod.Delete
                        "OPTIONS" -> HttpMethod.Options
                        "TRACE" -> HttpMethod.Trace
                        "PATCH" -> HttpMethod.Patch
                        "QUERY" -> HttpMethod.Query
                        else -> return@runBlocking null // HTTP Method is not supported by Ktor Client
                    }
                }

                return@runBlocking PlatformWebResourceResponse(
                    mimeType = res.contentType()?.contentType,
                    encoding = null,
                    statusCode = res.status.value,
                    reasonPhrase = null,
                    responseHeaders = res.headers
                        .toMap()
                        .map { it.key to it.value.joinToString(", ") }
                        .toMap(),
                    data = res.bodyAsBytes()
                )
            }
        }
    }

    ComposeWebView(
        state = webviewState,
        controller = controller,
        client = client,
        modifier = Modifier.fillMaxSize(),
        onCreated = { webview ->
            webview.javaScriptEnabled = true
        }
    )
}