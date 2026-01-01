package io.github.bommbomm34.intervirt.gui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable

@Composable
fun MultipleAnimatedVisibility(
    visible: Int,
    screens: List<@Composable (AnimatedVisibilityScope.() -> Unit)>
) {
    screens.forEachIndexed { i, it ->
        AnimatedVisibility(
            visible = i == visible,
            content = it
        )
    }
}