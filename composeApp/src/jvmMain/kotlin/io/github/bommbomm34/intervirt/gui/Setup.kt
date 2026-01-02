package io.github.bommbomm34.intervirt.gui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bommbomm34.intervirt.AGENT_PORT
import io.github.bommbomm34.intervirt.VM_SHUTDOWN_TIMEOUT
import io.github.bommbomm34.intervirt.data.AppConfigurationData
import io.github.bommbomm34.intervirt.data.Screens
import io.github.bommbomm34.intervirt.data.VMConfigurationData
import io.github.bommbomm34.intervirt.gui.components.*
import io.github.bommbomm34.intervirt.preferences
import java.io.File

@Composable
fun Setup() {
    var currentSetupScreenIndex by remember { mutableStateOf(Screens.Setup.VM_CONFIGURATION) }
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
                vmShutdownTimeout = VM_SHUTDOWN_TIMEOUT.toInt(),
                agentPort = AGENT_PORT,
                intervirtFolder = preferences.loadString("dataDir")
                    ?: (System.getProperty("user.home") + File.separator + "Intervirt")
            )
        )
    }
    val setupScreens: List<@Composable (AnimatedVisibilityScope.() -> Unit)> = listOf(
        { VMConfiguration(vmConf) { vmConf = it } },
        { AppConfiguration(appConf) { appConf = it } },
        { Installation() }
    )
    AlignedBox(Alignment.TopCenter, 32.dp) {
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
    AlignedBox(Alignment.BottomStart, 32.dp) {
        BackButton(currentSetupScreenIndex > 0) {
            currentSetupScreenIndex--
        }
    }
    AlignedBox(Alignment.BottomCenter, 32.dp) {
        Text(
            text = (currentSetupScreenIndex + 1).toString(),
            fontSize = 25.sp
        )
    }
    AlignedBox(Alignment.BottomEnd, 32.dp) {
        NextButton(currentSetupScreenIndex < setupScreens.size - 1) {
            currentSetupScreenIndex++
        }
    }
}