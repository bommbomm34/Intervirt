/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.configuration

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.AVAILABLE_LANGUAGES
import java.util.*

@Composable
fun LanguagePicker(
    language: Locale,
    onChangeLanguage: (Locale) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Button(onClick = { expanded = true }) {
            Text(language.displayLanguage)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AVAILABLE_LANGUAGES.forEach {
                DropdownMenuItem(
                    onClick = { onChangeLanguage(it) },
                    enabled = language.toLanguageTag() != it.toLanguageTag(),
                    text = { Text(it.displayLanguage) },
                )
            }
        }
    }
}