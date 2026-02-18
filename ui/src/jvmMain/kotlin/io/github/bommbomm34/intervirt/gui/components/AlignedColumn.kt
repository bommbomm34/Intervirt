package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun AlignedColumn(alignment: Alignment.Horizontal, content: @Composable ColumnScope.() -> Unit) = Column(
    horizontalAlignment = alignment,
    content = content,
)