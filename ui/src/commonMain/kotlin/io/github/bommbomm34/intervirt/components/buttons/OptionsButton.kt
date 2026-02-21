package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.options
import io.github.bommbomm34.intervirt.components.GeneralIcon
import org.jetbrains.compose.resources.stringResource

@Composable
fun OptionsButton(onClick: () -> Unit) {
    IconButton(onClick) {
        GeneralIcon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(Res.string.options),
        )
    }
}