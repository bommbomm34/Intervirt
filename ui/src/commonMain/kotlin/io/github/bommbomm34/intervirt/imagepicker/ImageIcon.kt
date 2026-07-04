/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.imagepicker

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.data.Image
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun ImageIcon(image: Image) {
    ComposeImage(
        painter = painterResource(image.icon),
        contentDescription = image.toReadableName(),
        modifier = Modifier.size(currentAppEnv.osIconSize.dp),
    )
}
