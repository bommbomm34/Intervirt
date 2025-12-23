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
import io.github.bommbomm34.intervirt.api.DeviceInterface
import io.github.bommbomm34.intervirt.api.QEMUInterface
import io.github.bommbomm34.intervirt.data.IntervirtConfiguration
import io.github.bommbomm34.intervirt.data.Executor
import io.github.bommbomm34.intervirt.data.FileManagement
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
                    val deviceInterface = DeviceInterface(
                        IntervirtConfiguration(
                            author = "Hi",
                            version = "alpha",
                            devices = mutableListOf()
                        ), fileManagement)
                    println(deviceInterface.generateIPv4())
                    println(deviceInterface.generateIPv6())
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