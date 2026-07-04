/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleProject
import io.github.bommbomm34.intervirt.core.singleSettings
import io.github.bommbomm34.intervirt.core.singleTestSettings
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.model.SettingsViewModel
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
import kotlin.test.assertNotEquals

class SettingsViewModelTest : KoinTest {
    private val appState: AppState by inject()
    private val appEnv: AppEnv get() = appState.env
    private val viewModel: SettingsViewModel by inject()

    @BeforeTest
    fun start() {
        startKoin {
            modules(
                module {
                    singleProject()
                    singleTestSettings()
                    singleAppEnvUpdater()
                    singleAppEnv()
                    single<AppState>()

                    viewModel<SettingsViewModel>()
                },
            )
        }
    }

    @Test
    fun shouldDiscardChangesIfNotSaved() {
        println(appEnv.virtualContainerIOPort)
        performChanges()
        println(appEnv.virtualContainerIOPort)
        assertNotEquals("MOCK", appEnv.overrideDockerHost)
        assertNotEquals(6767, appEnv.virtualContainerIOPort)
    }

    @Test
    fun shouldChangeAppEnvChangeKeyIfSaved() {
        performChanges()
        val previousAppEnvChangeKey = appState.appEnvChangeKey
        viewModel.saveChanges()
        assertNotEquals(previousAppEnvChangeKey, appState.appEnvChangeKey)
    }

    @Test
    fun shouldSaveChanges() {
        performChanges()
        val previousAppEnvChangeKey = appState.appEnvChangeKey
        viewModel.saveChanges()
        assertNotEquals(previousAppEnvChangeKey, appState.appEnvChangeKey)
        assertEquals("MOCK", appEnv.overrideDockerHost)
        assertEquals(6767, appEnv.virtualContainerIOPort)
    }

    private fun performChanges() {
        viewModel.appEnv = viewModel.appEnv.copy(
            overrideDockerHost = "MOCK",
            virtualContainerIOPort = 6767,
        )
    }

    @AfterTest
    fun stop() {
        stopKoin()
    }
}
