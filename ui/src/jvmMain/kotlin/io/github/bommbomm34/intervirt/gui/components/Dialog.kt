package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Copy
import intervirt.ui.generated.resources.*
import io.github.bommbomm34.intervirt.copyToClipboard
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.DialogState
import io.github.bommbomm34.intervirt.data.Importance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun Dialog() {
    val appState = koinInject<AppState>()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    AlignedBox(Alignment.Center) {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colors.background.copy(blue = 0.05f),
        ) {
            Box(Modifier.padding(16.dp)) {
                when (appState.dialogState) {
                    is DialogState.Custom -> (appState.dialogState as DialogState.Custom).customContent()
                    is DialogState.Regular -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                        ) {
                            val state = appState.dialogState as DialogState.Regular
                            Text(
                                text = when (state.importance) {
                                    Importance.INFO -> stringResource(Res.string.info)
                                    Importance.ERROR -> stringResource(Res.string.error)
                                    Importance.WARNING -> stringResource(Res.string.warning)
                                },
                                color = when (state.importance) {
                                    Importance.INFO -> MaterialTheme.colors.onSecondary
                                    Importance.ERROR -> MaterialTheme.colors.error
                                    Importance.WARNING -> Color.Yellow
                                },
                            )
                            GeneralSpacer()
                            SelectionContainer {
                                Text(state.message)
                            }
                            GeneralSpacer()
                            CenterRow {
                                Button(
                                    onClick = { appState.dialogState = state.copy(visible = false) },
                                ) {
                                    Text("OK")
                                }
                                GeneralSpacer()
                                // Copy button
                                IconButton(
                                    onClick = {
                                        scope.launch { clipboard.copyToClipboard(state.message) }
                                    },
                                ) {
                                    GeneralIcon(
                                        imageVector = TablerIcons.Copy,
                                        contentDescription = stringResource(Res.string.copy),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun CoroutineScope.launchDialogCatching(
    appState: AppState,
    block: suspend CoroutineScope.() -> Unit
) {
    launch {
        appState.runDialogCatching {
            block()
        }
    }
}