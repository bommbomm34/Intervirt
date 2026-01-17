package io.github.bommbomm34.intervirt.gui.components.configuration

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.bommbomm34.intervirt.CURRENT_VERSION
import io.github.bommbomm34.intervirt.api.DeviceManager
import kotlinx.coroutines.launch

@Composable
fun DebugOptions() {
    val scope = rememberCoroutineScope()
    Text("Debugging enabled")
    Text("Current version: $CURRENT_VERSION")
    Button(onClick = {
        scope.launch {
            DeviceManager.debug()
        }
    }) {
        Text("Debug Agent")
    }
}