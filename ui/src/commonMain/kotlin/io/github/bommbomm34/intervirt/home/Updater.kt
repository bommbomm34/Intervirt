/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.home

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.update
import io.github.bommbomm34.intervirt.components.CenterColumn
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.components.NamedCheckbox
import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.data.UpdaterState
import org.jetbrains.compose.resources.stringResource

@Composable
fun Updater(
    state: UpdaterState,
    onUpdate: () -> Unit,
) {
    CenterColumn {
        Downloader.Component.entries.forEach { component ->
            NamedCheckbox(
                checked = component in state.applyUpdates,
                onCheckedChange = {
                    if (it) {
                        state.applyUpdates += component
                    } else {
                        state.applyUpdates -= component
                    }
                },
                name = component.readableName,
                enabled = component in state.updates,
            )
        }
    }
    GeneralSpacer()
    // Update button
    Button(
        onClick = onUpdate,
    ) {
        Text(stringResource(Res.string.update))
    }
}
