package io.github.bommbomm34.intervirt.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.bommbomm34.intervirt.data.VMConfigurationData
import io.github.bommbomm34.intervirt.gui.components.VMConfiguration

@Composable
fun Setup() {
    var vmConf by remember {
        mutableStateOf(
            VMConfigurationData(
                ram = 2048,
                cpu = Runtime.getRuntime().availableProcessors() / 2,
                kvm = false
            )
        )
    }
    VMConfiguration(vmConf, onConfChange = { vmConf = it })
}