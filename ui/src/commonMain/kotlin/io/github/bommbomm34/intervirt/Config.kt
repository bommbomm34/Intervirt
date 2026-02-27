package io.github.bommbomm34.intervirt

import androidx.compose.ui.unit.Density
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.model.SettingsViewModel
import io.github.bommbomm34.intervirt.model.home.OptionDropdownViewModel
import io.github.bommbomm34.intervirt.model.setup.InstallationViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel
import java.util.*

const val CURRENT_VERSION = "0.0.1"
const val HELP_URL = "https://docs.perhof.org/intervirt"
const val HOMEPAGE_URL = "https://perhof.org/intervirt"

val AVAILABLE_LANGUAGES = listOf(
    Locale.US,
)

val uiModule = module {
    singleOf(::AppState)
    viewModel<OptionDropdownViewModel>()
    viewModel<SettingsViewModel>()
    viewModel<InstallationViewModel>()
}

lateinit var density: Density