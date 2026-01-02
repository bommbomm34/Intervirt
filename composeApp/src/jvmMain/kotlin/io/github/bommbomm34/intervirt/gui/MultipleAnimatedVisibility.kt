package io.github.bommbomm34.intervirt.gui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable

@Composable
fun MultipleAnimatedVisibility(
    visible: Int,
    screens: List<@Composable (AnimatedVisibilityScope.() -> Unit)>
) {
    screens.forEachIndexed { i, it ->
        AnimatedVisibility(
            visible = i == visible,
            enter = fadeIn(),
            exit = fadeOut(),
            content = it
        )
    }
}