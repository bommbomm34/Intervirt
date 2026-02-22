package io.github.bommbomm34.intervirt.components.tables

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import io.github.windedge.table.DataTable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClickableTable(
    headers: List<String>,
    data: List<List<@Composable BoxScope.() -> Unit>>,
    onClick: (Int) -> Unit, // Parameter is the index of data list
) {
    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground
    val scrollState = rememberScrollState()
    val hoverData = data.map {
        val interactionSource = remember { MutableInteractionSource() }
        interactionSource to interactionSource.collectIsHoveredAsState()
    }
    DataTable(
        columns = {
            headers.forEach {
                column { BoldText(it) }
            }
        },
        modifier = Modifier.verticalScroll(scrollState),
    ) {
        data.forEachIndexed { i, composables ->
            val hover = hoverData[i]
            row(
                Modifier
                    .onClick { onClick(i) }
                    .hoverable(hover.first)
                    .background(if (hover.second.value) onBg.copy(alpha = 0.5f) else bg)
                    .pointerHoverIcon(PointerIcon.Hand),
            ) {
                composables.forEach {
                    cell { it() }
                }
            }
        }
    }
}