package io.github.bommbomm34.intervirt.model.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import intervirt.ui.generated.resources.Res
import intervirt.ui.generated.resources.applying_configuration
import intervirt.ui.generated.resources.cancel
import intervirt.ui.generated.resources.creating_intervirt_folder
import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.data.AppState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.getString
import org.koin.core.annotation.KoinViewModel
import kotlin.invoke
import kotlin.times

@KoinViewModel
class InstallationViewModel(
    private val downloader: Downloader,
    private val appEnv: AppEnv,
    private val fileManager: FileManager,
    private val appState: AppState,
) : ViewModel() {
    var allowInstallation by mutableStateOf(false)
    var flow: Flow<ResultProgress<String>>? by mutableStateOf(null)
    var job: Job? by mutableStateOf(null)

    fun clickButton(){
        if (job != null) {
            job?.cancel()
            job = null
            flow = null
        } else {
            flow = flow {
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
                    if (it is ResultProgress.Result && it.result.isFailure) job!!.cancel()
                }
                // Downloading disk
                downloader.downloadAlpineDisk().collect {
                    emit(it.clone(percentage = it.percentage * 0.5f + 0.5f))
                    if (it is ResultProgress.Result && it.result.isFailure) job!!.cancel()
                }
                appEnv.INSTALLED = true
                appState.currentScreenIndex = 1
            }
        }
    }
}