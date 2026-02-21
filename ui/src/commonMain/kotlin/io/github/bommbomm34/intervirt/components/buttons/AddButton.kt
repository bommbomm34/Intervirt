package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.add
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddButton(
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = TablerIcons.Plus,
            contentDescription = stringResource(Res.string.add),
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}