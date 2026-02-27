package io.github.bommbomm34.intervirt.intervirtos.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bommbomm34.intervirt.components.dialogs.launchDialogCatching
import io.github.bommbomm34.intervirt.core.api.intervirtos.DnsResolverManager
import io.github.bommbomm34.intervirt.core.data.AppEnv
import io.github.bommbomm34.intervirt.core.data.dns.DnsRecord
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.intervirtos.DNS_RECORD_TYPES
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel


@KoinViewModel
class DnsResolverViewModel(
    @InjectedParam val dnsResolver: DnsResolverManager,
    private val appEnv: AppEnv,
    private val appState: AppState,
) : ViewModel() {
    var domain by mutableStateOf("perhof.org")
    var expanded by mutableStateOf(false)
    var dnsRecordType by mutableStateOf(DNS_RECORD_TYPES[0])
    var dnsServer by mutableStateOf(appEnv.DEFAULT_DNS_SERVER)
    var reverseLookup by mutableStateOf(false)
    val records = mutableStateListOf<DnsRecord>()

    fun lookup() {
        records.clear()
        viewModelScope.launchDialogCatching(appState) {
            records.addAll(
                dnsResolver.lookupDns(
                    name = domain,
                    type = dnsRecordType,
                    nameserver = dnsServer,
                    reverse = reverseLookup,
                ).getOrThrow(),
            )
        }
    }
}