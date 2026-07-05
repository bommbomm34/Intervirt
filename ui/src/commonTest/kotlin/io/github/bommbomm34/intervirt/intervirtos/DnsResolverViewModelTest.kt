/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.intervirtos

import io.github.bommbomm34.intervirt.core.api.ContainerIOClient
import io.github.bommbomm34.intervirt.core.api.Executor
import io.github.bommbomm34.intervirt.core.api.FileManager
import io.github.bommbomm34.intervirt.core.api.impl.VirtualContainerIOClient
import io.github.bommbomm34.intervirt.core.api.intervirtos.DnsResolverManager
import io.github.bommbomm34.intervirt.core.data.CommandStatus
import io.github.bommbomm34.intervirt.core.data.toCommandStatus
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.singleProject
import io.github.bommbomm34.intervirt.core.singleSettings
import io.github.bommbomm34.intervirt.core.singleTestSettings
import io.github.bommbomm34.intervirt.data.AppState
import io.github.bommbomm34.intervirt.intervirtos.model.DnsResolverViewModel
import io.github.bommbomm34.intervirt.singleAppEnvHolder
import io.github.bommbomm34.intervirt.singleTestAppState
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DnsResolverViewModelTest : KoinTest {
    private val viewModel: DnsResolverViewModel by inject { parametersOf(get<DnsResolverManager>()) }

    @BeforeTest
    fun start() {
        startKoin {
            modules(
                module {
                    single { getTestAppEnv() }
                    single { getHttpClient() }
                    singleProject()
                    singleTestSettings()
                    singleAppEnvHolder()
                    singleTestAppState()
                    single<MockExecutor>() bind Executor::class
                    single<FileManager>()
                    single<ContainerIOClient> {
                        VirtualContainerIOClient(
                            id = "mock-id",
                            wipeOnClose = true,
                            executor = get(),
                            fileManager = get(),
                        )
                    }
                    single<DnsResolverManager>()

                    viewModel<DnsResolverViewModel>()
                },
            )
        }
    }

    @Test
    fun shouldLookupARecord() = runTest {
        viewModel.domain = "one.one.one.one"
        viewModel.dnsRecordType = "A"
        viewModel.lookup().join()
        assertTrue {
            viewModel.records.any {
                it.name == "one.one.one.one." && it.type == "A" && it.dnsClass == "IN" && it.data == "1.1.1.1"
            }
        }
    }

    @Test
    fun shouldLookupAAAARecord() = runTest {
        viewModel.domain = "google.com"
        viewModel.dnsRecordType = "AAAA"
        viewModel.lookup().join()
        assertTrue {
            viewModel.records.any {
                it.name == "google.com." && it.type == "AAAA" && it.dnsClass == "IN" && it.data == "2a00:1450:4001:80d::200e"
            }
        }
    }

    @Test
    fun shouldLookupPTRRecordReverse() = runTest {
        viewModel.domain = "1.1.1.1"
        viewModel.dnsRecordType = "PTR"
        viewModel.reverseLookup = true
        viewModel.lookup().join()
        assertTrue {
            viewModel.records.any {
                it.name == "1.1.1.1.in-addr.arpa." && it.type == "PTR" && it.dnsClass == "IN" && it.data == "one.one.one.one."
            }
        }
    }

    @AfterTest
    fun stop() {
        stopKoin()
    }
}

class MockExecutor : Executor {
    override fun runCommand(workingFolder: PlatformFile?, commands: List<String>): Flow<CommandStatus> = flow {
        val text = when {
            commands.contains("A") -> LOOKUP_DNS_A
            commands.contains("AAAA") -> LOOKUP_DNS_AAAA
            commands.contains("PTR") -> LOOKUP_DNS_PTR_REVERSE
            else -> error("Invalid mock commands: $commands")
        }
        emit(text.toCommandStatus())
        emit(0.toCommandStatus())
    }
}

private const val LOOKUP_DNS_A = """
    {
  "responses": [
    {
      "answers": [
        {
          "name": "one.one.one.one.",
          "type": "A",
          "class": "IN",
          "ttl": "42879s",
          "address": "1.0.0.1",
          "status": "",
          "rtt": "519ms",
          "nameserver": "9.9.9.9:53"
        },
        {
          "name": "one.one.one.one.",
          "type": "A",
          "class": "IN",
          "ttl": "42879s",
          "address": "1.1.1.1",
          "status": "",
          "rtt": "519ms",
          "nameserver": "9.9.9.9:53"
        }
      ],
      "authorities": null,
      "questions": [
        {
          "name": "one.one.one.one.",
          "type": "A",
          "class": "IN"
        }
      ]
    }
  ]
}
"""

private const val LOOKUP_DNS_AAAA = """
    {
  "responses": [
    {
      "answers": [
        {
          "name": "google.com.",
          "type": "AAAA",
          "class": "IN",
          "ttl": "4s",
          "address": "2a00:1450:4001:80d::200e",
          "status": "",
          "rtt": "48ms",
          "nameserver": "9.9.9.9:53"
        }
      ],
      "authorities": null,
      "questions": [
        {
          "name": "google.com.",
          "type": "AAAA",
          "class": "IN"
        }
      ]
    }
  ]
}
"""

private const val LOOKUP_DNS_PTR_REVERSE = """
    {
  "responses": [
    {
      "answers": [
        {
          "name": "1.1.1.1.in-addr.arpa.",
          "type": "PTR",
          "class": "IN",
          "ttl": "940s",
          "address": "one.one.one.one.",
          "status": "",
          "rtt": "330ms",
          "nameserver": "9.9.9.9:53"
        }
      ],
      "authorities": null,
      "questions": [
        {
          "name": "1.1.1.1.in-addr.arpa.",
          "type": "PTR",
          "class": "IN"
        }
      ]
    }
  ]
}
"""
