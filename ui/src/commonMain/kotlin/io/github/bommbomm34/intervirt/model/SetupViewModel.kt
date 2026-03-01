package io.github.bommbomm34.intervirt.model

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.bommbomm34.intervirt.components.configuration.AppConfiguration
import io.github.bommbomm34.intervirt.components.configuration.VMConfiguration
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.setup.Installation
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SetupViewModel(
    appEnv: AppEnv
) : ViewModel() {
    val setupScreens: List<@Composable (AnimatedVisibilityScope.() -> Unit)> = listOf(
        { VMConfiguration(appEnv) },
        { AppConfiguration(appEnv) },
        { Installation() },
    )
    var currentSetupScreenIndex by mutableStateOf(0)
}