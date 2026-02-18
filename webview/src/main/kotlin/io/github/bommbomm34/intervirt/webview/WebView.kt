package io.github.bommbomm34.intervirt.webview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel

@Composable
fun WebView(
    url: String,
    navigator: WebViewNavigator,
    modifier: Modifier,
    proxy: Proxy? = null,
) {
    val panel = remember { WebViewPanel(url, proxy) }
    SwingPanel(
        background = MaterialTheme.colorScheme.background,
        factory = { panel },
        modifier = modifier,
    )
    LaunchedEffect(Unit) {
        with(navigator) {
            panel.handleNavigationEvents()
        }
    }
}