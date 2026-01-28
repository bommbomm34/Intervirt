package io.github.bommbomm34.intervirt.gui.components.buttons

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.X
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.close
import org.jetbrains.compose.resources.stringResource

@Composable
fun CloseButton(onClose: () -> Unit){
    IconButton(onClick = onClose) {
        Icon(
            imageVector = TablerIcons.X,
            contentDescription = stringResource(Res.string.close),
            tint = MaterialTheme.colors.onBackground
        )
    }
}