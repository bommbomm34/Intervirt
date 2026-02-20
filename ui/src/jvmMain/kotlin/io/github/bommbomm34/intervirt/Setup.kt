package io.github.bommbomm34.intervirt

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
    val preferences = koinInject<Preferences>()
    val isDarkMode = appEnv.isDarkMode()
    var currentSetupScreenIndex by remember { mutableStateOf(0) }
    var vmConf by remember {
        mutableStateOf(
            VMConfigurationData(
                ram = 2048,
                cpu = Runtime.getRuntime().availableProcessors() / 2,
                kvm = false,
                diskUrl = appEnv.VM_DISK_URL,
                diskHashUrl = appEnv.VM_DISK_HASH_URL,
            ),
        )
    }
    var appConf by remember {
        mutableStateOf(
            AppConfigurationData(
                vmShutdownTimeout = appEnv.VM_SHUTDOWN_TIMEOUT.toInt(),
                agentPort = appEnv.AGENT_PORT,
                intervirtFolder = appEnv.DATA_DIR.absolutePath,
                darkMode = isDarkMode,
                language = appEnv.LANGUAGE.toLanguageTag(),
            ),
        )
    }
    val setupScreens: List<@Composable (AnimatedVisibilityScope.() -> Unit)> = listOf(
        {
            _root_ide_package_.io.github.bommbomm34.intervirt.components.configuration.VMConfiguration(vmConf) {
                vmConf = it
            }
        },
        {
            _root_ide_package_.io.github.bommbomm34.intervirt.components.configuration.AppConfiguration(appConf) {
                appConf = it
            }
        },
        {
            _root_ide_package_.io.github.bommbomm34.intervirt.setup.Installation {
                appEnv.applyConfiguration(
                    vmConf,
                    appConf
                )
            }
        },
    )
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.TopCenter) {
        Text(
            text = "Intervirt Setup",
            fontSize = 40.sp,
        )
    }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.Center) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.MultipleAnimatedVisibility(
            visible = currentSetupScreenIndex,
            screens = setupScreens,
        )
    }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.BottomStart) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.BackButton(currentSetupScreenIndex > 0) {
            currentSetupScreenIndex--
        }
    }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.BottomCenter) {
        Text(
            text = (currentSetupScreenIndex + 1).toString(),
            fontSize = 25.sp,
        )
    }
    _root_ide_package_.io.github.bommbomm34.intervirt.components.AlignedBox(Alignment.BottomEnd) {
        _root_ide_package_.io.github.bommbomm34.intervirt.components.buttons.NextButton(currentSetupScreenIndex < setupScreens.size - 1) {
            currentSetupScreenIndex++
        }
    }
}