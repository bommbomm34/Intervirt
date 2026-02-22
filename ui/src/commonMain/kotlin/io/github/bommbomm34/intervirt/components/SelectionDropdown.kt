package io.github.bommbomm34.intervirt.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*


@Composable
fun SelectionDropdown(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach {
                DropdownMenuItem(
                    onClick = {
                        onSelect(it)
                        expanded = false
                    },
                    text = { Text(it) },
                )
            }
        }
        Button(onClick = { expanded = true }) {
            Text(selected)
        }
    }
}