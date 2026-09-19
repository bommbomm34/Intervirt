/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IpUtilsTest {
    @Test
    fun `should validate loopback IPv4`() {
        "0.0.0.0".validateIpv4().shouldBeTrue()
    }

    @Test
    fun `should validate loopback IPv6`() {
        "::1".validateIpv6().shouldBeTrue()
    }

    @Test
    fun `should validate IPv4 with too big integers`() {
        "0.0.0.256".validateIpv4().shouldBeFalse()
    }

    @Test
    fun `should validate IPv6 with non-hexadecimal numbers`() {
        "fd00:9999:9999:9999:9999:9999:kkkk:2222".validateIpv6().shouldBeFalse()
    }

    @Test
    fun `should validate IPv4 with too many parts`() {
        "0.0.0.0.0.0.0".validateIpv4().shouldBeFalse()
    }

    @Test
    fun `should validate IPv6 with too many parts`() {
        "fd00:0000:0000:0000:0000:0000:0000:0000:0000:0000".validateIpv6().shouldBeFalse()
    }

    @Test
    fun `should validate public IPv4 address`() {
        "142.251.36.110".validateIpv4().shouldBeTrue()
    }

    @Test
    fun `should validate public IPv6 address`() {
        "2a00:1450:4001:806::200e".validateIpv6().shouldBeTrue()
    }

    @Test
    fun `should validate MAC address`() {
        "ff:ff:ff:ff:ff:89".validateMac().shouldBeTrue()
    }

    @Test
    fun `should validate MAC address with too many parts`() {
        "ff:ff:ff:ff:ff:ff:ff".validateMac().shouldBeFalse()
    }

    @Test
    fun `should validate MAC address with non-hexadecimal numbers`() {
        "kk:kk:22:22:22:22".validateMac().shouldBeFalse()
    }
}
