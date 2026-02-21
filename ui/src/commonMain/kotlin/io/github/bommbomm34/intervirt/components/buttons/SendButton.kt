package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
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
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = stringResource(Res.string.send),
        )
    }
}