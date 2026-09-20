/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.terminal
import intervirt.ui.generated.resources.terminal_closed
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.ShellView
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import org.jetbrains.compose.resources.stringResource

@Composable
fun Terminal(
    osClient: IntervirtOSClient,
) {
    var closed by remember { mutableStateOf(false) }

    if (!closed) {
        ShellView(osClient.getClient().ioClient) {
            closed = true
        }
    } else {
        Text(stringResource(Res.string.terminal_closed))
    }
}
