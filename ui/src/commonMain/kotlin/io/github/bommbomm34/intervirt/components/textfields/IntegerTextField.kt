/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.components.textfields

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun IntegerTextField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) = OutlinedTextField(
    value = value.toString(),
    onValueChange = { newValue -> newValue.toIntOrNull()?.let { onValueChange(it) } },
    label = { Text(label) },
    modifier = modifier,
)