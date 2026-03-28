/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IpUtilsTest {
    private val testCount = System.getenv("INTERVIRT_ADDRESS_FUZZ_TEST_COUNT")?.toIntOrNull() ?: 1000

    @Test
    fun shouldValidateLoopbackIpv4() {
        assertTrue { "0.0.0.0".validateIpv4() }
    }

    @Test
    fun shouldValidateLoopbackIpv6() {
        assertTrue { "::1".validateIpv6() }
    }

    @Test
    fun shouldValidateIpv4WithTooBigIntegers() {
        assertFalse { "0.0.0.256".validateIpv4() }
    }

    @Test
    fun shouldValidateIpv6WithNonHexadecimalNumbers() {
        assertFalse { "fd00:9999:9999:9999:9999:9999:kkkk:2222".validateIpv6() }
    }

    @Test
    fun shouldValidateIpv4WithTooManyParts() {
        assertFalse { "0.0.0.0.0.0.0".validateIpv4() }
    }

    @Test
    fun shouldValidateIpv6WithTooManyParts() {
        assertFalse { "fd00:0000:0000:0000:0000:0000:0000:0000:0000:0000".validateIpv6() }
    }

    @Test
    fun shouldValidatePublicIpv4Address() {
        assertTrue { "142.251.36.110".validateIpv4() }
    }

    @Test
    fun shouldValidatePublicIpv6Address() {
        assertTrue { "2a00:1450:4001:806::200e".validateIpv6() }
    }

    @Test
    fun shouldValidateRandomValidIpv4Addresses() {
        repeat(testCount) {
            assertTrue { randomIpv4().validateIpv4() }
        }
    }

    @Test
    fun shouldValidateRandomValidIpv6Addresses() {
        repeat(testCount) {
            assertTrue { randomIpv6().validateIpv6() }
        }
    }

    @Test
    fun shouldValidateValidMacAddress(){
        assertTrue { "ff:ff:ff:ff:ff:89".validateMac() }
    }

    @Test
    fun shouldValidateMacAddressWithTooManyParts(){
        assertFalse { "ff:ff:ff:ff:ff:ff:ff".validateMac() }
    }

    @Test
    fun shouldValidateMacAddressWithNonHexadecimalNumbers(){
        assertFalse { "kk:kk:22:22:22:22".validateMac() }
    }

    @Test
    fun shouldValidateRandomValidMacAddresses(){
        repeat(testCount){
            assertTrue { randomMac().validateMac() }
        }
    }
}