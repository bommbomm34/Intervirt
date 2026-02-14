package io.github.bommbomm34.intervirt.core.data

data class VMConfigurationData(
    val ram: Int, // RAM in MB
    val cpu: Int,
    val kvm: Boolean, // Only available on Linux with root
    val diskUrl: String,
    val diskHashUrl: String
)
