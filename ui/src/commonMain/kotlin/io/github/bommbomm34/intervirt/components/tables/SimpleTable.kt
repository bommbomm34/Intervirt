package io.github.bommbomm34.intervirt.components.tables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SimpleTable(
    headers: List<String>,
    content: List<List<Any>>,
    customElements: List<@Composable () -> Unit> = emptyList(),
) {
    CustomTable(
        headers = headers,
        content = content.mapIndexed { i, row ->
            buildList {
                addAll(row.map { { Text(it.toString()) } })
                println("$i with $row: ${customElements.getOrNull(i)}")
                customElements.getOrNull(i)?.let { element -> add(element) }
            }
        },
    )
}