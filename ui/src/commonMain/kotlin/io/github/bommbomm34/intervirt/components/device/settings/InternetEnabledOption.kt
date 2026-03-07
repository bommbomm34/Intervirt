/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.device.settings

import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.internet_access
import io.github.bommbomm34.intervirt.components.NamedCheckbox
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import org.jetbrains.compose.resources.stringResource

@Composable
fun InternetEnabledOption(
    enabled: Boolean,
    onEnable: (Boolean) -> Unit,
) {
    NamedCheckbox(
        checked = enabled,
        onCheckedChange = onEnable,
        name = stringResource(Res.string.internet_access),
    )
}