package io.github.bommbomm34.intervirt.core.data

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class DeviceId(val value: String) {
    private val rawType: String
        get() = value.substringBefore("-")
    private val rawNumber: String
        get() = value.substringAfter("-")

    val type: DeviceType
        get() = DeviceType.entries.first { it.raw == rawType }
    val number: Int
        get() = value.substringAfter("-").toInt()

    init {
        // Parse structure
        if (DeviceType.entries.none { it.raw == rawType }) {
            throw IllegalArgumentException("Invalid device type: $rawType")
        }
        if (value.count { it == '-' } != 1) {
            throw IllegalArgumentException("Expected exactly one dash, but found: $value")
        }
        if (rawNumber.substringAfter("-").toIntOrNull() == null) {
            throw IllegalArgumentException("Expected integer after dash, but got: $rawNumber")
        }
    }

    override fun toString(): String {
        return value
    }
}
