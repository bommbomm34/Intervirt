/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IpUtilsTest {
    @Test
    fun `should validate loopback IPv4`() {
        assertTrue { "0.0.0.0".validateIpv4() }
    }

    @Test
    fun `should validate loopback IPv6`() {
        assertTrue { "::1".validateIpv6() }
    }

    @Test
    fun `should validate IPv4 with too big integers`() {
        assertFalse { "0.0.0.256".validateIpv4() }
    }

    @Test
    fun `should validate IPv6 with non-hexadecimal numbers`() {
        assertFalse { "fd00:9999:9999:9999:9999:9999:kkkk:2222".validateIpv6() }
    }

    @Test
    fun `should validate IPv4 with too many parts`() {
        assertFalse { "0.0.0.0.0.0.0".validateIpv4() }
    }

    @Test
    fun `should validate IPv6 with too many parts`() {
        assertFalse { "fd00:0000:0000:0000:0000:0000:0000:0000:0000:0000".validateIpv6() }
    }

    @Test
    fun `should validate public IPv4 address`() {
        assertTrue { "142.251.36.110".validateIpv4() }
    }

    @Test
    fun `should validate public IPv6 address`() {
        assertTrue { "2a00:1450:4001:806::200e".validateIpv6() }
    }

    @Test
    fun `should validate MAC address`() {
        assertTrue { "ff:ff:ff:ff:ff:89".validateMac() }
    }

    @Test
    fun `should validate MAC address with too many parts`() {
        assertFalse { "ff:ff:ff:ff:ff:ff:ff".validateMac() }
    }

    @Test
    fun `should validate MAC address with non-hexadecimal numbers`() {
        assertFalse { "kk:kk:22:22:22:22".validateMac() }
    }
}
