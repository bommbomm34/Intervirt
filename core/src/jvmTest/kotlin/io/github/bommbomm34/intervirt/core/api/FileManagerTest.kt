/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.injectAppEnv
import io.github.bommbomm34.intervirt.core.singleAppEnvHolder
import io.github.bommbomm34.intervirt.core.util.runIntervirtTest
import io.github.vinceglb.filekit.*
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.test.KoinTest
import org.koin.test.inject
import java.io.File
import kotlin.test.*

class FileManagerTest : KoinTest {
    private val appEnv: AppEnv by injectAppEnv()
    private val fileManager: FileManager by inject()

    @BeforeTest
    fun init() {
        startKoin {
            modules(
                module {
                    singleAppEnvHolder()
                    single { getHttpClient() }
                    single<FileManager>()
                },
            )
        }
    }

    @Test
    fun `should initialize successfully`() = runIntervirtTest {
        fileManager.init()
        val files = appEnv.actualDataDir.list().map { it.name }
        files shouldContain "qemu"
        files shouldContain "disk"
        files shouldContain "cache"
    }

    @Test
    fun `should download file`() = runIntervirtTest {
        fileManager.init()
        var finishedSuccessfully = false
        fileManager.downloadFile(DOWNLOAD_URL, "license").collect {
            when (it) {
                is ResultProgress.Result<PlatformFile> -> {
                    val res = it.result
                    res.shouldBeRight()
                    val file = res.value
                    file.exists().shouldBeTrue()
                    file.readString() shouldContain "GNU"
                    finishedSuccessfully = true
                }

                else -> {}
            }
        }
        finishedSuccessfully.shouldBeTrue()
    }

    @Test
    fun `should extract ZIP`() = runIntervirtTest {
        val tempFolder = fileManager.getFile("cache/temp-folder")
        val file = PlatformFile(File(javaClass.getResource("/hello.zip")!!.file))
        fileManager.extractZip(file, tempFolder)
        val files = tempFolder.list()
        val hello: PlatformFile? = files.firstOrNull { it.name == "hello.txt" }
        hello.shouldNotBeNull()
        hello.readString() shouldContain "Hello World"
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    companion object {
        const val DOWNLOAD_URL = "https://raw.githubusercontent.com/bommbomm34/Intervirt/refs/heads/main/LICENSE"
    }
}
