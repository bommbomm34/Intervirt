package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import io.github.bommbomm34.intervirt.data.AppEnv
import io.github.bommbomm34.intervirt.isDarkMode
import io.github.windedge.table.DataTable
import org.koin.compose.koinInject

@Composable
fun SimpleTable(
    headers: List<String>,
    content: List<List<Any>>,
    customElements: List<@Composable () -> Unit> = emptyList()
){
    SelectionContainer {
        DataTable(
            columns = {
                headerBackground {
                    Box(Modifier.background(MaterialTheme.colorScheme.onBackground))
                }
                headers.forEach {
                    column { VisibleText(it, true) }
                }
            }
        ){
            content.forEachIndexed { i, row ->
                row {
                    row.forEach {
                        cell {
                            VisibleText(it)
                        }
                    }
                    customElements.getOrNull(i)?.let { cell { it() } }
                }
            }
        }
    }
}

@Composable
private fun VisibleText(text: Any, bold: Boolean = false){
    val appEnv = koinInject<AppEnv>()
    Text(
        text = text.toString(),
        color = if (appEnv.isDarkMode()) Color.White else Color.Black,
        fontWeight = if (bold) FontWeight.ExtraBold else null
    )
}