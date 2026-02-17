package io.github.bommbomm34.intervirt.gui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.applyConfiguration
import io.github.bommbomm34.intervirt.core.api.Preferences
import io.github.bommbomm34.intervirt.core.data.AppConfigurationData
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.VMConfigurationData
import io.github.bommbomm34.intervirt.gui.components.AlignedBox
import io.github.bommbomm34.intervirt.gui.components.MultipleAnimatedVisibility
import io.github.bommbomm34.intervirt.gui.components.buttons.BackButton
import io.github.bommbomm34.intervirt.gui.components.buttons.NextButton
import io.github.bommbomm34.intervirt.gui.components.configuration.AppConfiguration
import io.github.bommbomm34.intervirt.gui.components.configuration.VMConfiguration
import io.github.bommbomm34.intervirt.gui.setup.Installation
import io.github.bommbomm34.intervirt.isDarkMode
import org.koin.compose.koinInject

@Composable
fun Setup() {
    val appEnv = koinInject<AppEnv>()
    val preferences = koinInject <Preferences>()
    val isDarkMode = appEnv.isDarkMode()
    var currentSetupScreenIndex by remember { mutableStateOf(0) }
    var vmConf by remember {
        mutableStateOf(
            VMConfigurationData(
                ram = 2048,
                cpu = Runtime.getRuntime().availableProcessors() / 2,
                kvm = false,
                diskUrl = appEnv.vmDiskUrl,
                diskHashUrl = appEnv.vmDiskHashUrl
            )
        )
    }
    var appConf by remember {
        mutableStateOf(
            AppConfigurationData(
                vmShutdownTimeout = appEnv.vmShutdownTimeout.toInt(),
                agentPort = appEnv.agentPort,
                intervirtFolder = appEnv.dataDir.absolutePath,
                darkMode = isDarkMode,
                language = appEnv.language.toLanguageTag()
            )
        )
    }
    val setupScreens: List<@Composable (AnimatedVisibilityScope.() -> Unit)> = listOf(
        { VMConfiguration(vmConf) { vmConf = it } },
        { AppConfiguration(appConf) { appConf = it } },
        { Installation { appEnv.applyConfiguration(vmConf, appConf) } }
    )
    AlignedBox(Alignment.TopCenter) {
        Text(
            text = "Intervirt Setup",
            fontSize = 40.sp
        )
    }
    AlignedBox(Alignment.Center) {
        MultipleAnimatedVisibility(
            visible = currentSetupScreenIndex,
            screens = setupScreens
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
            fontSize = 25.sp
        )
    }
    AlignedBox(Alignment.BottomEnd) {
        NextButton(currentSetupScreenIndex < setupScreens.size - 1) {
            currentSetupScreenIndex++
        }
    }
}