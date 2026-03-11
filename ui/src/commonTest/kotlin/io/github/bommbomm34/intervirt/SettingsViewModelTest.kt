/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt

import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.Project
import io.github.bommbomm34.intervirt.core.getTestAppEnv
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
    private val appEnv: AppEnv by inject()
    private val viewModel: SettingsViewModel by inject()
    
    @BeforeTest
    fun start(){
        startKoin { 
            modules(
                module { 
                    single { Project.default() }
                    single { getTestAppEnv() }
                    single<AppState>()

                    viewModel<SettingsViewModel>()
                }
            )
        }
    }
    
    @Test
    fun shouldDiscardChangesIfNotSaved(){
        performChanges()
        assertNotEquals("MOCK", appEnv.OVERRIDE_DOCKER_HOST)
        assertNotEquals(6767, appEnv.VIRTUAL_CONTAINER_IO_PORT)
    }

    @Test
    fun shouldChangeAppEnvChangeKeyIfSaved(){
        performChanges()
        val previousAppEnvChangeKey = appState.appEnvChangeKey
        viewModel.saveChanges()
        assertNotEquals(previousAppEnvChangeKey, appState.appEnvChangeKey)
    }

    @Test
    fun shouldSaveChanges(){
        performChanges()
        val previousAppEnvChangeKey = appState.appEnvChangeKey
        viewModel.saveChanges()
        assertNotEquals(previousAppEnvChangeKey, appState.appEnvChangeKey)
        assertEquals("MOCK", appEnv.OVERRIDE_DOCKER_HOST)
        assertEquals(6767, appEnv.VIRTUAL_CONTAINER_IO_PORT)
    }

    private fun performChanges(){
        viewModel.appEnv.OVERRIDE_DOCKER_HOST = "MOCK"
        viewModel.appEnv.VIRTUAL_CONTAINER_IO_PORT = 6767
    }
    
    @AfterTest
    fun stop(){
        stopKoin()
    }
}