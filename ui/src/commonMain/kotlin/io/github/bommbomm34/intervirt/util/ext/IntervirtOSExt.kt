/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.util.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.dialogs.ProgressDialog
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.ProxyManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerBasedManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.openDialog
import io.github.bommbomm34.intervirt.data.showFailureDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

fun CoroutineScope.initDocker(
    appState: AppState,
    manager: DockerBasedManager,
    onInitialize: () -> Unit,
) {
    launchDialogCatching(appState) {
        appState.openDialog {
            ProgressDialog(
                flow = manager.init(),
                showMessages = true,
                onClose = ::close,
                onMessage = { progress ->
                    if (progress is ResultProgress.Result) {
                        progress.result.fold(
                            ifRight = { onInitialize() },
                            ifLeft = {
                                launch {
                                    appState.showFailureDialog(it)
                                }
                            },
                        )
                    }
                },
            )
        }
    }
}

@Composable
fun rememberProxyManager(
    appEnv: AppEnv,
    deviceManager: DeviceManager,
    osClient: IntervirtOSClient,
) = remember(osClient) { ProxyManager(appEnv, deviceManager, osClient) }

@Composable
fun DockerBasedManager.initialize(): MutableState<Boolean> {
    val appState = koinInject<AppState>()
    val initialized = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    CatchingLaunchedEffect {
        appState.openDialog {
            ProgressDialog(
                flow = init(),
                showMessages = true,
                onClose = ::close,
                onMessage = { progress ->
                    if (progress is ResultProgress.Result) {
                        progress.result.fold(
                            ifRight = { initialized.value = true },
                            ifLeft = {
                                scope.launch {
                                    appState.showFailureDialog(it)
                                }
                            },
                        )
                    }
                },
            )
        }
    }
    return initialized
}

@Composable
fun <T> IntervirtOSClient.rememberManager(func: (AppEnv, IntervirtOSClient) -> T): T {
    val appEnv = koinInject<AppEnv>()
    return remember { func(appEnv, this) }
}
