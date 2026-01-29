package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.*
import androidx.compose.ui.awt.SwingPanel
import com.jediterm.terminal.ui.JediTermWidget
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.terminal_init_failed
import io.github.bommbomm34.intervirt.api.ContainerTtyConnector
import io.github.bommbomm34.intervirt.api.Executor
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import java.awt.Component
import javax.swing.JOptionPane

@Composable
fun Terminal(
    computer: ViewDevice.Computer
) {
    val executor = koinInject<Executor>()
    var terminalFactory: (() -> Component)? by remember { mutableStateOf(null) }
    LaunchedEffect(computer) {
        withContext(Dispatchers.IO) {
            executor.getContainerSession(computer.id).fold(
                onSuccess = { session ->
                    val connector = ContainerTtyConnector(session)
                    terminalFactory = {
                        val settingsProvider = DefaultSettingsProvider()
                        val terminal = JediTermWidget(settingsProvider)
                        terminal.ttyConnector = connector
                        terminal.start()
                        terminal
                    }
                },
                onFailure = {
                    JOptionPane.showMessageDialog(
                        null,
                        it.localizedMessage,
                        getString(Res.string.terminal_init_failed),
                        JOptionPane.ERROR
                    )
                }
            )
        }
    }
    terminalFactory?.let {
        SwingPanel(
            factory = it
        )
    }
}