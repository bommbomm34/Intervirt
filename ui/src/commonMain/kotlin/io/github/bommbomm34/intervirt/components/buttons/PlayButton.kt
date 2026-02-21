package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPlay
import compose.icons.tablericons.PlayerStop
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.start
import intervirt.ui.generated.resources.stop
import io.github.bommbomm34.intervirt.components.GeneralIcon
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlayButton(
    playing: Boolean,
    onClick: (Boolean) -> Unit,
) {
    IconButton(
        onClick = { onClick(!playing) },
    ) {
        GeneralIcon(
            imageVector = if (playing) TablerIcons.PlayerStop else TablerIcons.PlayerPlay,
            contentDescription = stringResource(if (playing) Res.string.stop else Res.string.start),
        )
    }
}