package io.github.bommbomm34.intervirt

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.components.AlignedBox
import io.github.bommbomm34.intervirt.components.MultipleAnimatedVisibility
import io.github.bommbomm34.intervirt.components.buttons.BackButton
import io.github.bommbomm34.intervirt.components.buttons.NextButton
import io.github.bommbomm34.intervirt.components.configuration.AppConfiguration
import io.github.bommbomm34.intervirt.components.configuration.VMConfiguration
import io.github.bommbomm34.intervirt.core.api.Preferences
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.setup.Installation
import org.koin.compose.koinInject

@Composable
fun Setup() {
    val appEnv = koinInject<AppEnv>()
    var currentSetupScreenIndex by remember { mutableStateOf(0) }
    val setupScreens: List<@Composable (AnimatedVisibilityScope.() -> Unit)> = listOf(
        { VMConfiguration(appEnv) },
        { AppConfiguration(appEnv) },
        { Installation() },
    )
    AlignedBox(Alignment.TopCenter) {
        Text(
            text = "Intervirt Setup",
            fontSize = 40.sp,
        )
    }
    AlignedBox(Alignment.Center) {
        MultipleAnimatedVisibility(
            visible = currentSetupScreenIndex,
            screens = setupScreens,
        )
    }
    AlignedBox(Alignment.BottomStart) {
        BackButton(currentSetupScreenIndex > 0) {
            currentSetupScreenIndex--
        }
    }
    AlignedBox(Alignment.BottomCenter) {
        Text(
            text = (currentSetupScreenIndex + 1).toString(),
            fontSize = 25.sp,
        )
    }
    AlignedBox(Alignment.BottomEnd) {
        NextButton(currentSetupScreenIndex < setupScreens.size - 1) {
            currentSetupScreenIndex++
        }
    }
}