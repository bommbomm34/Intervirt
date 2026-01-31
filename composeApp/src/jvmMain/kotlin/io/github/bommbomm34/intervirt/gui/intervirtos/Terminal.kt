package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.api.Executor
import io.github.bommbomm34.intervirt.data.stateful.ViewDevice
import org.koin.compose.koinInject

@Composable
fun Terminal(
    computer: ViewDevice.Computer
) {
    val executor = koinInject<Executor>()
    // TODO: Open terminal separate process
}