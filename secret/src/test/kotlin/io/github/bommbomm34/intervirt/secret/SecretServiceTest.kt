/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.secret

import uniffi.secret.SecretServiceException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull

class SecretServiceTest {
    val data = byteArrayOf(0, 1, 2, 3)
    private val secretService = getMockSecretService()

    @Test
    fun shouldAddEntry(){
        println(data.clone().joinToString())
        secretService.setEntry("my-test-entry", data.clone()).getOrThrow()
        assertContentEquals(data, getTestEntry())
    }

    @Test
    fun shouldSetEntryTwice(){
        val otherData = byteArrayOf(2, 8, 9, 1)
        secretService.setEntry("my-test-entry", data.clone()).getOrThrow()
        secretService.setEntry("my-test-entry", otherData.clone()).getOrThrow()
        assertContentEquals(otherData, getTestEntry())
    }

    @Test
    fun shouldDeleteEntry(){
        secretService.setEntry("my-test-entry", data.clone()).getOrThrow()
        secretService.removeEntry("my-test-entry").getOrThrow()
        assertNull(getTestEntry())
    }

    @Test
    fun shouldReturnNullWhenEntryNotExists(){
        assertNull(secretService.getEntry("non-existing-entry").getOrThrow())
    }

    @Test
    fun shouldZeroizeAfterAdd(){
        val data = byteArrayOf(5, 0, 1, 2)
        secretService.setEntry("my-test-entry", data)
        assertContentEquals(byteArrayOf(0, 0, 0, 0), data)
    }

    @AfterTest
    fun cleanup(){
        secretService.removeEntry("my-test-entry").muteNoEntry().getOrThrow()
    }

    private fun getTestEntry() = secretService.getEntry("my-test-entry").getOrThrow()
}

private fun Result<Unit>.muteNoEntry() = recoverCatching {
    if (it !is SecretServiceException.NoEntry) throw it
}