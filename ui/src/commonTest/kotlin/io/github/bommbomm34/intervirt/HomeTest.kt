/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import io.github.bommbomm34.intervirt.core.api.Downloader
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.api.GuestManager
import io.github.bommbomm34.intervirt.core.api.QemuClient
import io.github.bommbomm34.intervirt.core.api.impl.VirtualGuestManager
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.getAppEnv
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.singleProject
import io.github.bommbomm34.intervirt.core.singleSettings
import io.github.bommbomm34.intervirt.core.singleTestSettings
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.data.Screen
import io.github.bommbomm34.intervirt.model.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeTest : KoinTest {
    val viewModel: HomeViewModel by inject()
    val appState: AppState by inject()

    @BeforeTest
    fun init() {
        startKoin {
            modules(
                module {
                    single<AppState>()
                    single { getAppEnv() }
                    single { getHttpClient() }
                    single<GuestManager> { VirtualGuestManager() }
                    singleProject()
                    singleTestSettings()
                    singleAppEnvHolder()
                    single<FileManager>()
                    single<QemuClient>()
                    single<Downloader>()
                    viewModel<HomeViewModel>()
                },
            )
        }
    }

    @Test
    fun shouldChangeDeviceRenderKey() {
        viewModel.devicesViewRenderKey = 0
        viewModel.onConfChange()
        assertEquals(1, viewModel.devicesViewRenderKey)
    }

    @Test
    fun getZoom() {
        assertEquals("1.0x", viewModel.getZoom())
    }

    @Test
    fun shouldOpenSettings() {
        viewModel.openSettings()
        assertEquals(Screen.SETTINGS, appState.currentScreen)
    }

    @Test
    fun shouldOpenAbout() {
        viewModel.openAbout()
        assertEquals(Screen.ABOUT, appState.currentScreen)
    }

    @Test
    fun shouldDismiss() {
        viewModel.onDismiss()
        assertEquals(false, viewModel.showOptions)
    }

    @AfterTest
    fun stop() {
        stopKoin()
    }
}
