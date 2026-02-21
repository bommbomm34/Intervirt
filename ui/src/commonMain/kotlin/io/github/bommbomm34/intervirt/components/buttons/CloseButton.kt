package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.X
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.close
import org.jetbrains.compose.resources.stringResource

@Composable
fun CloseButton(onClose: () -> Unit) {
    IconButton(onClick = onClose) {
        Icon(
            imageVector = TablerIcons.X,
            contentDescription = stringResource(Res.string.close),
        )
    }
}