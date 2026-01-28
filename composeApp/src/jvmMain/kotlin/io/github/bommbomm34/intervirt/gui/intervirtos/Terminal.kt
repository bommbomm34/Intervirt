package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.*
import androidx.compose.ui.awt.SwingPanel
import com.jediterm.terminal.ui.JediTermWidget
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.awt.Component

@Composable
fun Terminal(
    computer: ViewDevice.Computer
) {
    val deviceManager = koinInject<DeviceManager>()
    var terminalFactory: (() -> Component)? by remember { mutableStateOf(null) }
    LaunchedEffect(computer) {
        withContext(Dispatchers.IO) {
            val connector = deviceManager.getTtyConnector(computer.device)
            terminalFactory = {
                val settingsProvider = DefaultSettingsProvider()
                val terminal = JediTermWidget(settingsProvider)
                terminal.ttyConnector = connector
                terminal.start()
                terminal
            }
        }
    }
    terminalFactory?.let {
        SwingPanel(
            factory = it
        )
    }
}