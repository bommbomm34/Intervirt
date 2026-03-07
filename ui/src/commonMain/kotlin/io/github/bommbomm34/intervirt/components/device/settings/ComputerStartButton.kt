/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.runtime.Composable
import io.github.bommbomm34.intervirt.components.buttons.PlayButton
import io.github.bommbomm34.intervirt.data.ViewDevice

@Composable
fun ComputerStartButton(
    device: ViewDevice.Computer,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    PlayButton(device.running) {
        if (it) {
            onStart()
        } else {
            onStop()
        }
    }
}