package io.github.bommbomm34.intervirt.gui.components.configuration

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.bommbomm34.intervirt.CURRENT_VERSION
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.openDialog
import kotlinx.coroutines.launch

@Composable
fun DebugOptions() {
    val scope = rememberCoroutineScope()
    Text("Debugging enabled")
    Text("Current version: $CURRENT_VERSION")
    Button(onClick = {
        openDialog(
            importance = Importance.INFO,
            message = "Debug with: ./gradlew"
        )
    }) {
        Text("Debug Agent")
    }
}