package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun AlignedBox(
    alignment: Alignment,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) = Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = alignment,
    content = content
)

@Composable
fun AlignedBox(
    alignment: Alignment,
    padding: Dp,
    content: @Composable BoxScope.() -> Unit
) = AlignedBox(alignment, Modifier.padding(padding), content)