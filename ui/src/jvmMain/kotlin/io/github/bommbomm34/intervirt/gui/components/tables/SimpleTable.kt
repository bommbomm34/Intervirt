package io.github.bommbomm34.intervirt.gui.components.tables

import androidx.compose.runtime.Composable

@Composable
fun SimpleTable(
    headers: List<String>,
    content: List<List<Any>>,
    customElements: List<@Composable () -> Unit> = emptyList()
){
    CustomTable(
        headers = headers,
        content = content.mapIndexed { i, row ->
            buildList {
                addAll(row.map { { VisibleText(it) } })
                customElements.getOrNull(i)?.let { element -> add(element) }
            }
        }
    )
}