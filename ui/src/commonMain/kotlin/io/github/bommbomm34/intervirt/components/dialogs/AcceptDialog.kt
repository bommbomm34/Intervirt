/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.no
import intervirt.ui.generated.resources.yes
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.DialogState
import io.github.bommbomm34.intervirt.data.openDialog
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AcceptDialog(
    message: String,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp),
    ) {
        Text(message)
        GeneralSpacer()
        Row {
            Button(
                onClick = {
                    onAccept()
                },
            ) {
                Text(stringResource(Res.string.yes))
            }
            GeneralSpacer()
            Button(
                onClick = {
                    onCancel()
                },
            ) {
                Text(stringResource(Res.string.no))
            }
        }
    }
}

fun AppState.openAcceptDialog(
    res: StringResource,
    vararg formatArgs: String,
    onAccept: DialogState.() -> Unit,
) = openDialog {
    AcceptDialog(
        message = stringResource(res, *formatArgs),
        onCancel = ::close,
        onAccept = { onAccept() },
    )
}
