package io.github.bommbomm34.intervirt.gui.components.configuration

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.bommbomm34.intervirt.AVAILABLE_LANGUAGES
import java.util.Locale

@Composable
fun LanguagePicker(
    language: Locale,
    onChangeLanguage: (Locale) -> Unit
){
    var expanded by remember { mutableStateOf(false) }
    Column {
        Button(onClick = { expanded = true }){
            Text(language.displayLanguage)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ){
            AVAILABLE_LANGUAGES.forEach {
                DropdownMenuItem(
                    onClick = { onChangeLanguage(it) },
                    enabled = language.toLanguageTag() != it.toLanguageTag()
                ){
                    Text(it.displayLanguage)
                }
            }
        }
    }
}