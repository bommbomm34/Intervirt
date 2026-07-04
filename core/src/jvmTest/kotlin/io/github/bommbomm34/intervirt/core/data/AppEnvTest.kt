/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.serialization.decodeValueOrNull
import io.github.bommbomm34.intervirt.core.data.env.storeEnv
import io.github.bommbomm34.intervirt.core.getAppEnv
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.util.toAtomic
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
class AppEnvTest {
    private val map = mutableMapOf<String, Any>()
    private val settings = MapSettings(map)
    private val _appEnv = getTestAppEnv(settings).toAtomic()
    private val appEnv by _appEnv

    @Test
    fun shouldNotSaveReallyPersistent() {
        _appEnv.update { it.copy(overrideDockerHost = "MOCK") }
        val realAppEnv = getAppEnv()
        assertNotEquals("MOCK", realAppEnv.overrideDockerHost)
    }

    @Test
    fun shouldSaveTemporarily() {
        _appEnv.update { it.copy(overrideDockerHost = "MOCK") }
        assertEquals("MOCK", appEnv.overrideDockerHost)
    }

    @Test
    fun shouldSavePersistent() {
        _appEnv.update { it.copy(overrideDockerHost = "MOCK") }
        settings.storeEnv(appEnv)
        val otherAppEnv = getTestAppEnv(settings)
        assertEquals("MOCK", otherAppEnv.overrideDockerHost)
        assertEquals("MOCK", map["overrideDockerHost"])
    }

    @Test
    fun shouldGetDefault() {
        assertEquals(true, appEnv.debugEnabled)
    }
}
