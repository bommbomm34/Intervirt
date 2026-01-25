package io.github.bommbomm34.intervirt.data.qemu

import kotlinx.serialization.Serializable

@Serializable
data class QemuRequestBody (
    val execute: String
)