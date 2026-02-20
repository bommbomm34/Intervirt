package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPlay
import compose.icons.tablericons.PlayerStop
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.start
import intervirt.ui.generated.resources.stop
import io.github.bommbomm34.intervirt.gui.components.GeneralIcon
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlayButton(
    playing: Boolean,
    onClick: (Boolean) -> Unit,
) {
    IconButton(
        onClick = { onClick(!playing) },
    ) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.GeneralIcon(
            imageVector = if (playing) TablerIcons.PlayerStop else TablerIcons.PlayerPlay,
            contentDescription = stringResource(if (playing) Res.string.stop else Res.string.start),
        )
    }
}