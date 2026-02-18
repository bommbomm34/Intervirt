package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.no
import intervirt.ui.generated.resources.yes
import io.github.bommbomm34.intervirt.data.AppState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AcceptDialog(
    message: String,
    onCancel: () -> Unit = {},
    onAccept: () -> Unit,
) {
    val appState = koinInject<AppState>()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message)
        GeneralSpacer()
        Row {
            Button(
                onClick = {
                    appState.closeDialog()
                    onAccept()
                },
            ) {
                Text(stringResource(Res.string.yes))
            }
            GeneralSpacer()
            Button(
                onClick = {
                    appState.closeDialog()
                    onCancel()
                },
            ) {
                Text(stringResource(Res.string.no))
            }
        }
    }
}