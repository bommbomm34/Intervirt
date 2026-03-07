package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.getAppEnv

fun getTestAppEnv() = getAppEnv {
    VIRTUAL_AGENT_MODE = System.getenv("INTERVIRT_TEST_VIRTUAL_AGENT_MODE")?.toBoolean() ?: true
    VIRTUAL_CONTAINER_IO = System.getenv("INTERVIRT_TEST_VIRTUAL_CONTAINER_IO")?.toBoolean() ?: true
}