package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.DotsVertical
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.options
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import org.jetbrains.compose.resources.stringResource

@Composable
fun OptionsButton(onClick: () -> Unit) {
    IconButton(onClick) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralIcon(
            imageVector = TablerIcons.DotsVertical,
            contentDescription = stringResource(Res.string.options),
        )
    }
}