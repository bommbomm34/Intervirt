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
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun Updater(
    state: UpdaterState,
    onUpdate: () -> Unit,
) {
    CenterColumn {
        state.updates.forEach { component ->
            NamedCheckbox(
                checked = state.applyUpdates.contains(component),
                onCheckedChange = {
                    if (it) state.applyUpdates.add(component) else state.applyUpdates.remove(component)
                },
                name = component.readableName,
            )
            GeneralSpacer()
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