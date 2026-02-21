package io.github.bommbomm34.intervirt.components.buttons

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.components.CenterRow
import io.github.bommbomm34.intervirt.components.GeneralIcon
import io.github.bommbomm34.intervirt.components.GeneralSpacer

@Composable
fun IconText(
    imageVector: ImageVector,
    text: String,
) {
    CenterRow {
        GeneralIcon(
            imageVector = imageVector,
            contentDescription = text,
        )
        GeneralSpacer(2.dp)
        Text(text)
    }
}