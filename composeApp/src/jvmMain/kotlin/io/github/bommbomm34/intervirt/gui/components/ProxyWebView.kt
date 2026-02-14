package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.runtime.*
import androidx.compose.ui.awt.SwingPanel
import io.github.bommbomm34.intervirt.core.data.Address
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.rememberLogger
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import org.koin.compose.koinInject
import java.awt.Component

/**
 * WebView with HTTP proxy support. Uses JavaFX in background.
 */
@Composable
fun ProxyWebView(
    url: String,
    proxyAddress: Address
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    val logger = rememberLogger("ProxyWebView")
    val env = koinInject<AppEnv>()
    val jfxPanel = remember { JFXPanel() }
    Platform.runLater {
        webView = WebView().also {
            it.engine.isJavaScriptEnabled = env.enableJavaScript
            jfxPanel.scene = Scene(it)
            it.engine.load(url)
        }
    }
    val factory: () -> Component = remember {
        {
            setProxy(proxyAddress)
            jfxPanel
        }
    }

    SwingPanel(
        factory = factory
    )

    LaunchedEffect(url) {
        webView?.let {
            logger.debug { "Loading $url" }
            Platform.runLater { it.engine.load(url) }
        }
    }
}

private fun setProxy(address: Address) {
    System.setProperty("http.proxy", address.host)
    System.setProperty("http.proxyPort", address.port.toString())
    System.setProperty("https.proxy", address.host)
    System.setProperty("https.proxyPort", address.port.toString())
}