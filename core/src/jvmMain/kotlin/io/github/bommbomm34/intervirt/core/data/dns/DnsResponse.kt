package io.github.bommbomm34.intervirt.core.data.dns

import kotlinx.serialization.Serializable

@Serializable
data class DnsResponse(
    val answers: List<DnsRecordAnswer>,
)
