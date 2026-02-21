package io.github.bommbomm34.intervirt.intervirtos.mail.client

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.github.bommbomm34.intervirt.core.data.mail.MailConnectionSafety

@Composable
fun MailSafetyChooser(
    protocol: String,
    safety: MailConnectionSafety,
    onSafetyChange: (MailConnectionSafety) -> Unit,
) {
    val mailSafeties = listOf(
        protocol to MailConnectionSafety.NONE,
        "STARTTLS" to MailConnectionSafety.STARTTLS,
        "${protocol}S" to MailConnectionSafety.SECURE,
    )
    var expanded by remember { mutableStateOf(false) }
    Column {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            mailSafeties.forEach { mailSafety ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSafetyChange(mailSafety.second)
                    },
                    text = { Text(mailSafety.first) },
                )
            }
        }
        Button(
            onClick = { expanded = true },
        ) {
            Text(mailSafeties.first { it.second == safety }.first)
        }
    }
}