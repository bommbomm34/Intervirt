package io.github.bommbomm34.intervirt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import intervirt.ui.generated.resources.Res
import io.github.bommbomm34.intervirt.components.CatchingLaunchedEffect
import io.github.bommbomm34.intervirt.components.dialogs.ProgressDialog
import io.github.bommbomm34.intervirt.core.api.DeviceManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.ProxyManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.DockerBasedManager
import io.github.bommbomm34.intervirt.core.api.intervirtos.general.IntervirtOSClient
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.data.AppState
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.*
import org.koin.compose.koinInject
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.ServerSocket
import java.util.*

const val CURRENT_VERSION = "0.0.1"
const val HELP_URL = "https://docs.perhof.org/intervirt"
const val HOMEPAGE_URL = "https://perhof.org/intervirt"

val AVAILABLE_LANGUAGES = listOf(
    Locale.US,
)

val uiModule = module {
    singleOf(::AppState)
}

lateinit var density: Density