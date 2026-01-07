package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import intervirt.composeapp.generated.resources.Res
import intervirt.composeapp.generated.resources.error
import intervirt.composeapp.generated.resources.info
import intervirt.composeapp.generated.resources.warning
import io.github.bommbomm34.intervirt.data.DialogState
import io.github.bommbomm34.intervirt.data.Importance
import io.github.bommbomm34.intervirt.dialogState
import org.jetbrains.compose.resources.stringResource

@Composable
fun Dialog(customContent: (@Composable ColumnScope.() -> Unit)? = null) {
    AnimatedVisibility(dialogState.visible) {
        Surface(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            AlignedBox(Alignment.Center) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colors.secondary
                ) {
                    CenterColumn {
                        if (customContent != null) customContent() else {
                            Text(
                                text = when (dialogState.importance) {
                                    Importance.INFO -> stringResource(Res.string.info)
                                    Importance.ERROR -> stringResource(Res.string.error)
                                    Importance.WARNING -> stringResource(Res.string.warning)
                                },
                                color = when (dialogState.importance) {
                                    Importance.INFO -> MaterialTheme.colors.onSecondary
                                    Importance.ERROR -> MaterialTheme.colors.error
                                    Importance.WARNING -> Color.Yellow
                                }
                            )
                            GeneralSpacer()
                            Text(dialogState.message)
                            GeneralSpacer()
                            Button(
                                onClick = { dialogState = dialogState.copy(visible = false) }
                            ){
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }
}