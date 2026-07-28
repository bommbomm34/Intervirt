/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.are_you_sure_to_remove_device
import intervirt.ui.generated.resources.delete
import intervirt.ui.generated.resources.name
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.components.dialogs.openAcceptDialog
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.data.AppState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun GeneralDeviceSettings(
    device: Device,
    onNameChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    OutlinedTextField(
        value = device.id.value,
        onValueChange = {}, // ID can't be changed once set
        enabled = false,
        label = { Text("ID") },
    )
    GeneralSpacer()
    OutlinedTextField(
        value = device.name,
        onValueChange = onNameChange,
        label = { Text(stringResource(Res.string.name)) },
    )
    GeneralSpacer()
    Button(
        onClick = onDelete,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
    ) {
        Text(
            text = stringResource(Res.string.delete),
            color = Color.White,
        )
    }
}
