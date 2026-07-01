/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.imagepicker

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import intervirt.ui.generated.resources.Res
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Image
import io.github.bommbomm34.intervirt.data.showFailureDialog
import io.github.bommbomm34.intervirt.rememberLogger
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject

@Composable
fun ImageIcon(image: Image) {
    val appEnv = koinInject<AppEnv>()

    ComposeImage(
        painter = painterResource(image.icon),
        contentDescription = image.toReadableName(),
        modifier = Modifier.size(appEnv.OS_ICON_SIZE.dp),
    )
}
