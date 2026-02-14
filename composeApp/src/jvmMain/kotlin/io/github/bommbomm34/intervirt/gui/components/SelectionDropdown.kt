package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*


@Composable
fun SelectionDropdown(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ){
        options.forEach {
            DropdownMenuItem(
                onClick = {
                    onSelect(it)
                    expanded = false
                }
            ){
                Text(it)
            }
        }
    }
    Button(onClick = { expanded = true }){
        Text(selected)
    }
}