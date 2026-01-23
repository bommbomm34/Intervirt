package io.github.bommbomm34.intervirt.gui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.applyConfiguration
import io.github.bommbomm34.intervirt.data.AppConfigurationData
import io.github.bommbomm34.intervirt.api.Preferences
import io.github.bommbomm34.intervirt.data.VMConfigurationData
import io.github.bommbomm34.intervirt.gui.components.*
import io.github.bommbomm34.intervirt.gui.components.buttons.BackButton
import io.github.bommbomm34.intervirt.gui.components.buttons.NextButton
import io.github.bommbomm34.intervirt.gui.components.configuration.AppConfiguration
import io.github.bommbomm34.intervirt.gui.components.configuration.VMConfiguration
import io.github.bommbomm34.intervirt.gui.setup.Installation
import io.github.bommbomm34.intervirt.isDarkMode
import org.koin.compose.koinInject
import java.io.File

@Composable
fun Setup() {
    val preferences = koinInject<Preferences>()
    val isDarkMode = preferences.isDarkMode()
    var currentSetupScreenIndex by remember { mutableStateOf(0) }
    var vmConf by remember {
        mutableStateOf(
            VMConfigurationData(
                ram = 2048,
                cpu = Runtime.getRuntime().availableProcessors() / 2,
                kvm = false
            )
        )
    }
    var appConf by remember {
        mutableStateOf(
            AppConfigurationData(
                vmShutdownTimeout = preferences.VM_SHUTDOWN_TIMEOUT.toInt(),
                agentPort = preferences.AGENT_PORT,
                intervirtFolder = preferences.env("dataDir")
                    ?: (System.getProperty("user.home") + File.separator + "Intervirt"),
                darkMode = isDarkMode,
                language = preferences.LANGUAGE.toLanguageTag()
            )
        )
    }
    val setupScreens: List<@Composable (AnimatedVisibilityScope.() -> Unit)> = listOf(
        { VMConfiguration(vmConf) { vmConf = it } },
        { AppConfiguration(appConf) { appConf = it } },
        { Installation { preferences.applyConfiguration(vmConf, appConf) } }
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