package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val TABLE_CELL_WEIGHT = 0.5f

@Composable
fun Table(
    header: List<String>,
    data: List<List<@Composable () -> Unit>>
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(data.size)
    ) {
        item(header) {
            CenterRow {
                header.forEach {
                    Text(
                        text = it,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colors.onBackground)
                            .padding(8.dp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        items(data) { row ->
            CenterRow {
                row.forEach { TableCell(it) }
            }
        }
    }
}

@Composable
private fun RowScope.TableCell(
    content: @Composable () -> Unit
) = CenterColumn(
    Modifier
        .weight(TABLE_CELL_WEIGHT)
        .border(1.dp, MaterialTheme.colors.onBackground)
        .padding(8.dp)
) { content() }