package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GeneralSpacer(
    space: Dp = 16.dp
) = Spacer(Modifier.padding(space))