package io.github.bommbomm34.intervirt.core.data.qemu

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QmpErrorBody(
    @SerialName("class")
    val errorClass: String,
    @SerialName("desc")
    val description: String,
)
