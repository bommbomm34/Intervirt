package io.github.bommbomm34.intervirt.gui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import io.github.bommbomm34.intervirt.data.Preferences
import io.github.bommbomm34.intervirt.isDarkMode
import io.github.bommbomm34.intervirt.mousePosition
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DefaultWindowScope(
    onPointerEvent: AwaitPointerEventScope.(PointerEvent) -> Unit = {},
    content: @Composable BoxScope.() -> Unit
){
    val preferences = koinInject<Preferences>()
    val colors = if (preferences.isDarkMode()) darkColors() else lightColors()
    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography.copy(
            h1 = MaterialTheme.typography.h1.copy(colors.onBackground),
            h2 = MaterialTheme.typography.h2.copy(colors.onBackground),
            h3 = MaterialTheme.typography.h3.copy(colors.onBackground),
            h4 = MaterialTheme.typography.h4.copy(colors.onBackground),
            h5 = MaterialTheme.typography.h5.copy(colors.onBackground),
            h6 = MaterialTheme.typography.h6.copy(colors.onBackground),
            subtitle1 = MaterialTheme.typography.subtitle1.copy(colors.onBackground),
            subtitle2 = MaterialTheme.typography.subtitle2.copy(colors.onBackground),
            body1 = MaterialTheme.typography.body1.copy(colors.onBackground),
            body2 = MaterialTheme.typography.body2.copy(colors.onBackground),
            button = MaterialTheme.typography.button.copy(colors.onPrimary),
            caption = MaterialTheme.typography.caption.copy(colors.onBackground),
            overline = MaterialTheme.typography.overline.copy(colors.onBackground)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .background(colors.background)
                .onPointerEvent(
                    eventType = PointerEventType.Move,
                    onEvent = onPointerEvent
                ),
            content = content
        )
    }
}