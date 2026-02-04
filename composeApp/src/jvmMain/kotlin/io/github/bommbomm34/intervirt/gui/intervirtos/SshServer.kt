package io.github.bommbomm34.intervirt.gui.intervirtos

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import io.github.bommbomm34.intervirt.api.IntervirtOSClient
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.buttons.PlayButton
import kotlinx.coroutines.launch

@Composable
fun SshServer(
    osClient: IntervirtOSClient
){
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    AlignedBox(Alignment.TopEnd){
        PlayButton(running){
            scope.launch {
                osClient.enableSshServer(it)
            }
        }
    }
}