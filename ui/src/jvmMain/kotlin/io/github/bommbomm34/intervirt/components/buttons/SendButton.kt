package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.Send
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.send
import io.github.bommbomm34.intervirt.components.GeneralIcon
import org.jetbrains.compose.resources.stringResource

@Composable
fun SendButton(
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        GeneralIcon(
            imageVector = TablerIcons.Send,
            contentDescription = stringResource(Res.string.send),
        )
    }
}