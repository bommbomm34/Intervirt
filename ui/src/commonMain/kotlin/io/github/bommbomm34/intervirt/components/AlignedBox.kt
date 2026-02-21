package io.github.bommbomm34.intervirt.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AlignedBox(
    alignment: Alignment,
    padding: Dp = if (alignment != Alignment.Center) 16.dp else 0.dp,
    content: @Composable BoxScope.() -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .padding(padding),
    contentAlignment = alignment,
    content = content,
)