package io.github.bommbomm34.intervirt.gui.intervirtos.http

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.destination_folder
import intervirt.composeapp.generated.resources.domain
import io.github.bommbomm34.intervirt.data.VirtualHost
import io.github.bommbomm34.intervirt.gui.components.GeneralSpacer
import io.github.bommbomm34.intervirt.gui.components.buttons.AddButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddVirtualHostView(onAdd: (VirtualHost) -> Unit) {
    var serverName by remember { mutableStateOf("example.com") }
    var documentRoot by remember { mutableStateOf("/var/www/html") }
    // ServerName TextField
    OutlinedTextField(
        value = serverName,
        onValueChange = { serverName = it },
        label = { Text(stringResource(Res.string.domain)) }
    )
    GeneralSpacer()
    // DocumentRoot TextField
    OutlinedTextField(
        value = documentRoot,
        onValueChange = { documentRoot = it },
        label = { Text(stringResource(Res.string.destination_folder)) }
    )
    GeneralSpacer()
    AddButton {
        onAdd(VirtualHost(serverName, documentRoot))
        // Clear text fields
        serverName = ""
        documentRoot = ""
    }
}