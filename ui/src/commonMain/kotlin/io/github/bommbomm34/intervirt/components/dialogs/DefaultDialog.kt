package io.github.bommbomm34.intervirt.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.components.CenterRow
import io.github.bommbomm34.intervirt.components.GeneralIcon
import io.github.bommbomm34.intervirt.components.GeneralSpacer
import io.github.bommbomm34.intervirt.copyToClipboard
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Severity
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun DefaultDialog(
    message: String,
    severity: Severity,
    copyMessage: String,
    onClose: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = when (severity) {
                Severity.INFO -> stringResource(Res.string.info)
                Severity.ERROR -> stringResource(Res.string.error)
                Severity.WARNING -> stringResource(Res.string.warning)
            },
            color = when (severity) {
                Severity.INFO -> MaterialTheme.colorScheme.onSecondary
                Severity.ERROR -> MaterialTheme.colorScheme.error
                Severity.WARNING -> Color.Yellow
            },
        )
        GeneralSpacer()
        SelectionContainer {
            Text(message)
        }
        GeneralSpacer()
        CenterRow {
            Button(
                onClick = onClose,
            ) {
                Text("OK")
            }
            GeneralSpacer()
            // Copy button
            IconButton(
                onClick = {
                    scope.launch { clipboard.copyToClipboard(copyMessage) }
                },
            ) {
                GeneralIcon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(Res.string.copy),
                )
            }
        }
    }
}