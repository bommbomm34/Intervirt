package io.github.bommbomm34.intervirt.intervirtos.http

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.destination_folder
import intervirt.ui.generated.resources.domain
import io.github.bommbomm34.intervirt.core.data.VirtualHost
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
        label = { Text(stringResource(Res.string.domain)) },
    )
    _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
    // DocumentRoot TextField
    OutlinedTextField(
        value = documentRoot,
        onValueChange = { documentRoot = it },
        label = { Text(stringResource(Res.string.destination_folder)) },
    )
    _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralSpacer()
    _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.AddButton {
        onAdd(VirtualHost(serverName, documentRoot))
        // Clear text fields
        serverName = ""
        documentRoot = ""
    }
}