package io.github.bommbomm34.intervirt.components

import androidx.compose.material3.*
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TooltipArea(
    tooltipText: String,
    content: @Composable () -> Unit,
){
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.End),
        state = rememberTooltipState(),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        }
    ) {
        content()
    }
}

@Composable
fun TooltipArea(
    toolTipText: StringResource,
    content: @Composable () -> Unit,
) = TooltipArea(stringResource(toolTipText), content)
