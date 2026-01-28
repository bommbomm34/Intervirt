package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.animation.*
import androidx.compose.runtime.Composable

@Composable
fun MultipleAnimatedVisibility(
    visible: Int,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    screens: List<@Composable (AnimatedVisibilityScope.() -> Unit)>
) {
    screens.forEachIndexed { i, it ->
        AnimatedVisibility(
            visible = i == visible,
            enter = enter,
            exit = exit,
            content = it
        )
    }
}