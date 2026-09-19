/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.MapSettings
import io.github.bommbomm34.intervirt.core.data.env.storeEnv
import io.github.bommbomm34.intervirt.core.getAppEnv
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.util.toAtomic
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AppEnvTest {
    private val map = mutableMapOf<String, Any>()
    private val settings = MapSettings(map)
    private val _appEnv = getTestAppEnv(settings).toAtomic()
    private val appEnv by _appEnv

    @Test
    fun `should not save really persistent`() {
        _appEnv.update { it.copy(overrideDockerHost = "MOCK") }
        val realAppEnv = getAppEnv()
        realAppEnv.overrideDockerHost shouldNotBeEqual "MOCK"
    }

    @Test
    fun `should save temporarily`() {
        _appEnv.update { it.copy(overrideDockerHost = "MOCK") }
        appEnv.overrideDockerHost shouldBeEqual "MOCK"
    }

    @Test
    fun `should save persistent`() {
        _appEnv.update { it.copy(overrideDockerHost = "MOCK") }
        settings.storeEnv(appEnv)
        val otherAppEnv = getTestAppEnv(settings)
        otherAppEnv.overrideDockerHost shouldBeEqual "MOCK"
        map["overrideDockerHost"] shouldEqual "MOCK"
    }

    @Test
    fun `should get default`() {
        assertEquals(true, appEnv.debugEnabled)
    }
}
