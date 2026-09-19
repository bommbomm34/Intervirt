/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.model

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.applying_configuration
import intervirt.ui.generated.resources.creating_intervirt_folder
import io.github.bommbomm34.intervirt.components.configuration.AppConfiguration
import io.github.bommbomm34.intervirt.components.configuration.VMConfiguration
import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.api.atomic.AppEnvHolder
import io.github.bommbomm34.intervirt.core.api.atomic.getValue
import io.github.bommbomm34.intervirt.core.api.atomic.setValue
import io.github.bommbomm34.intervirt.core.data.Failure
import io.github.bommbomm34.intervirt.core.data.OS
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.getOS
import io.github.bommbomm34.intervirt.core.util.ext.flowCatching
import io.github.bommbomm34.intervirt.currentAppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Screen
import io.github.bommbomm34.intervirt.hasGroupMembership
import io.github.bommbomm34.intervirt.setup.Installation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SetupViewModel(
    private val downloader: Downloader,
    envHolder: AppEnvHolder,
    private val fileManager: FileManager,
    private val appState: AppState,
) : ViewModel() {
    var appEnv by envHolder
    val setupScreens: List<@Composable (AnimatedVisibilityScope.() -> Unit)> = listOf(
        { VMConfiguration(currentAppEnv) { appEnv = it } },
        { AppConfiguration(currentAppEnv) { appEnv = it } },
        { Installation(this@SetupViewModel) },
    )
    var allowInstallation by mutableStateOf(false)
    var flow: Flow<ResultProgress<String>>? by mutableStateOf(null)
    var job: Job? by mutableStateOf(null)
    var currentSetupScreenIndex by mutableIntStateOf(0)

    fun onInstall() {
        if (job != null) {
            job?.cancel()
            job = null
            flow = null
        } else {
            flow = flowCatching {
                // Check KVM access
                if (appEnv.vmEnableKvm) checkKvmAccess()
                // Creating Intervirt folder
                emit(
                    ResultProgress.proceed(
                        percentage = 0.05f,
                        message = getString(Res.string.creating_intervirt_folder),
                    ),
                )
                fileManager.init()
                // Applying configuration
                emit(
                    ResultProgress.proceed(
                        percentage = 0.1f,
                        message = getString(Res.string.applying_configuration),
                    ),
                )
                // Apply configuration
                // Downloading QEMU
                downloader.downloadQemu().collect {
                    emit(it.clone(percentage = it.percentage * 0.4f + 0.1f))
                    if (it is ResultProgress.Result && it.result.isLeft()) job!!.cancel()
                }
                // Downloading disk
                downloader.downloadAlpineDisk().collect {
                    emit(it.clone(percentage = it.percentage * 0.5f + 0.5f))
                    if (it is ResultProgress.Result && it.result.isLeft()) job!!.cancel()
                }
                appEnv = appEnv.copy(installed = true)
                appState.currentScreen = Screen.HOME
            }
        }
    }

    context(_: Raise<Failure>)
    private fun checkKvmAccess() {
        // Check OS
        if (getOS() != OS.LINUX) raise(
            Failure.InvalidOperatingSystem(
                os = OS.CURRENT,
                operation = "enable KVM",
            ),
        )
        // Check if user is root
        if (!hasGroupMembership("kvm")) {
            val username = System.getProperty("user.name")?.let { " '$it'" } ?: ""

            raise(Failure.MissingPermissions("Current user$username is not a member of group 'kvm'."))
        }
    }
}
