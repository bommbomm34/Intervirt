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
import io.github.bommbomm34.intervirt.data.Preferences
import io.github.bommbomm34.intervirt.data.Screens
import io.github.bommbomm34.intervirt.data.VMConfigurationData
import io.github.bommbomm34.intervirt.env
import io.github.bommbomm34.intervirt.gui.components.*
import java.io.File

@Composable
fun Setup(
    onFinish: () -> Unit
) {
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
                intervirtFolder = env("dataDir")
                    ?: (System.getProperty("user.home") + File.separator + "Intervirt")
            )
        )
    }
    val setupScreens: List<@Composable (AnimatedVisibilityScope.() -> Unit)> = listOf(
        { VMConfiguration(vmConf) { vmConf = it } },
        { AppConfiguration(appConf) { appConf = it } },
        { Installation(onFinish) { applyConfiguration(vmConf, appConf) } }
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

fun applyConfiguration(vmConf: VMConfigurationData, appConf: AppConfigurationData){
    Preferences.saveString("VM_RAM", vmConf.ram.toString())
    Preferences.saveString("VM_CPU", vmConf.cpu.toString())
    Preferences.saveString("VM_ENABLE_KVM", vmConf.kvm.toString())
    Preferences.saveString("VM_SHUTDOWN_TIMEOUT", appConf.vmShutdownTimeout.toString())
    Preferences.saveString("AGENT_PORT", appConf.agentPort.toString())
    Preferences.saveString("DATA_DIR", appConf.intervirtFolder)
}