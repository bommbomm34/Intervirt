package io.github.bommbomm34.intervirt.gui.components.configuration

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.bommbomm34.intervirt.CURRENT_VERSION
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.data.stateful.AppState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun DebugOptions() {
    val appState = koinInject<AppState>()
    Text("Debugging enabled")
    Text("Current version: $CURRENT_VERSION")
    Button(onClick = {
        appState.openDialog(
            importance = Importance.INFO,
            message = "Debug with: ./gradlew"
        )
    }) {
        Text("Debug Agent")
    }
}