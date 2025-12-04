package io.github.bommbomm34.intervirt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.bommbomm34.intervirt.api.QEMUInterface
import io.github.bommbomm34.intervirt.data.Executor
import io.github.bommbomm34.intervirt.data.FileManagement
import io.github.bommbomm34.intervirt.setup.Downloader
import io.github.bommbomm34.intervirt.setup.Tester
import io.ktor.util.logging.Logger
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
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
                    val tester = Tester(fileManagement, executor)
                    println(tester.testQEMUInstallation())
                    println("Guest: " + tester.testAlpineLinuxBoot())
                    tester.testDocker()
                }
            }) {
                Text("Test all")
            }
        }
    }
}