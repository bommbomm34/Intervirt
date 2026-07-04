/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.api

import arrow.core.Either
import io.github.bommbomm34.intervirt.core.data.env.AppEnv
import io.github.bommbomm34.intervirt.core.data.ResultProgress
import io.github.bommbomm34.intervirt.core.getHttpClient
import io.github.bommbomm34.intervirt.core.getTestAppEnv
import io.github.bommbomm34.intervirt.core.injectAppEnv
import io.github.bommbomm34.intervirt.core.singleAppEnvHolder
import io.github.bommbomm34.intervirt.core.util.runIntervirtTest
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.test.KoinTest
import org.koin.test.inject
import java.io.File
import kotlin.test.*

const val DOWNLOAD_URL = "https://raw.githubusercontent.com/bommbomm34/Intervirt/refs/heads/main/LICENSE"

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
    fun shouldInitSuccessfully() = runIntervirtTest {
        fileManager.init()
        val files = appEnv.actualDataDir.list().map { it.name }
        assertContains(files, "qemu")
        assertContains(files, "disk")
        assertContains(files, "cache")
    }

    @Test
    fun shouldDownloadFile() = runIntervirtTest {
        fileManager.init()
        var finishedSuccessfully = false
        fileManager.downloadFile(DOWNLOAD_URL, "license").collect {
            when (it) {
                is ResultProgress.Result<PlatformFile> -> {
                    val res = it.result
                    assertIs<Either.Right<PlatformFile>>(res)
                    val file = res.value
                    assertTrue(file.exists())
                    assertContains(file.readString(), "GNU")
                    finishedSuccessfully = true
                }

                else -> {}
            }
        }
        assertTrue(finishedSuccessfully)
    }

    @Test
    fun shouldExtractZip() = runIntervirtTest {
        val tempFolder = fileManager.getFile("cache/temp-folder")
        val file = PlatformFile(File(javaClass.getResource("/hello.zip")!!.file))
        fileManager.extractZip(file, tempFolder)
        val files = tempFolder.list()
        val hello: PlatformFile? = files.firstOrNull { it.name == "hello.txt" }
        assertNotNull(hello)
        assertContains(hello.readString(), "Hello World")
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }
}
