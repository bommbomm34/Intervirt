/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.serialization.decodeValueOrNull
import io.github.bommbomm34.intervirt.core.getAppEnv
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
class AppEnvTest {
    private val map = mutableMapOf<String, Any>()
    private val settings = MapSettings(map)
    private val appEnv = getTestAppEnv(settings)

    @Test
    fun shouldNotSaveReallyPersistent() {
        appEnv.OVERRIDE_DOCKER_HOST = "MOCK"
        val realAppEnv = getAppEnv()
        assertNotEquals("MOCK", realAppEnv.OVERRIDE_DOCKER_HOST)
    }

    @Test
    fun shouldSaveTemporarily() {
        appEnv.OVERRIDE_DOCKER_HOST = "MOCK"
        assertEquals("MOCK", appEnv.OVERRIDE_DOCKER_HOST)
    }

    @Test
    fun shouldSavePersistent() {
        appEnv.OVERRIDE_DOCKER_HOST = "MOCK"
        val otherAppEnv = getTestAppEnv(settings)
        assertEquals("MOCK", otherAppEnv.OVERRIDE_DOCKER_HOST)
        assertEquals("MOCK", map["OVERRIDE_DOCKER_HOST"])
    }

    @Test
    fun shouldGetDefault() {
        assertEquals(true, appEnv.DEBUG_ENABLED)
    }
}