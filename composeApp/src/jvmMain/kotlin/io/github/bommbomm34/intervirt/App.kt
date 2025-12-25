package io.github.bommbomm34.intervirt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.bommbomm34.intervirt.api.AgentInterface
import io.github.bommbomm34.intervirt.api.DeviceManagement
import io.github.bommbomm34.intervirt.api.QEMUInterface
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.data.Executor
import io.github.bommbomm34.intervirt.data.FileManagement
import io.github.bommbomm34.intervirt.data.IncusImage
import io.github.bommbomm34.intervirt.setup.Downloader
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File

val dataDir = File("${System.getProperty("user.home")}/Intervirt").apply { mkdir() }
val fileManagement = FileManagement(dataDir)
val executor = Executor(fileManagement)
val downloader = Downloader(fileManagement)
val qemu = QEMUInterface(fileManagement, executor)

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var content by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                scope.launch {
                    downloader.downloadQEMU().collect { println(it) }
                    downloader.downloadAlpineDisk().collect { println(it) }
                }
            }) {
                Text("Download all")
            }
            Button(onClick = {
                scope.launch {
                    val conf = IntervirtConfiguration(
                        author = "Max Mustermann",
                        version = "6.7",
                        devices = mutableListOf(),
                        connections = mutableListOf()
                    )
                    val management = DeviceManagement(conf, AgentInterface(fileManagement))
                    management.addComputer("John's Machine", 76f, 67f, IncusImage("debian", "12"))
                }
            }) {
                Text("Test all")
            }
            OutlinedTextField(value = content, onValueChange = { content = it })
            Button(onClick = {
                scope.launch {

                }
            }) {
                Text("Send command")
            }
        }
    }
}