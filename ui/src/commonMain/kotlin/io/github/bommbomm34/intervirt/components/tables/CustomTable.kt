package io.github.bommbomm34.intervirt.components.tables

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.isDarkMode
import io.github.windedge.table.DataTable
import org.koin.compose.koinInject

@Composable
fun CustomTable(
    headers: List<String>,
    content: List<List<@Composable () -> Unit>>,
) {
    require((content.maxOfOrNull { it.size } ?: headers.size) == headers.size) {
        "Largest content row size is not equal to headers size!"
    }
    SelectionContainer {
        DataTable(
            columns = {
                headers.forEach {
                    column { BoldText(it) }
                }
            },
        ) {
            content.forEachIndexed { i, row ->

                row {
                    row.forEach {
                        cell {
                            it()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoldText(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.ExtraBold,
    )
}