/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.os
import io.github.bommbomm34.intervirt.core.data.Device
import org.jetbrains.compose.resources.stringResource

@Composable
fun OSField(device: Device.Computer) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = device.image,
            onValueChange = {},
            label = { Text(stringResource(Res.string.os)) },
            enabled = false,
        )
    }
}
