package io.github.bommbomm34.intervirt.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import com.materialkolor.rememberDynamicColorScheme
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.isDarkMode
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DefaultWindowScope(
    onPointerEvent: AwaitPointerEventScope.(PointerEvent) -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val appEnv = koinInject<AppEnv>()
    val colors = rememberDynamicColorScheme(
        seedColor = Color(appEnv.ACCENT_COLOR),
        isDark = appEnv.isDarkMode(),
    )
    MaterialExpressiveTheme(
        colorScheme = colors,
    ) {
        Surface(contentColor = colors.onBackground) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeContentPadding()
                    .background(colors.background)
                    .onPointerEvent(
                        eventType = PointerEventType.Move,
                        onEvent = onPointerEvent,
                    ),
                content = content,
            )
        }
    }
}