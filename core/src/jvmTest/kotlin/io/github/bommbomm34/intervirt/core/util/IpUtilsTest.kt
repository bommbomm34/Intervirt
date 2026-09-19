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
        validateIpv4("0.0.0.0").shouldBeTrue()
    }

    @Test
    fun `should validate IPv4 with too big integers`() {
        validateIpv4("0.0.0.256").shouldBeFalse()
    }

    @Test
    fun `should validate IPv4 with too many parts`() {
        validateIpv4("0.0.0.0.0.0.0").shouldBeFalse()
    }

    @Test
    fun `should validate public IPv4 address`() {
        validateIpv4("142.251.36.110").shouldBeTrue()
    }

    @Test
    fun `should validate loopback IPv6`() {
        validateIpv6("::1").shouldBeTrue()
    }

    @Test
    fun `should validate IPv6 with non-hexadecimal numbers`() {
        validateIpv6("fd00:9999:9999:9999:9999:9999:kkkk:2222").shouldBeFalse()
    }

    @Test
    fun `should validate IPv6 with too many parts`() {
        validateIpv6("fd00:0000:0000:0000:0000:0000:0000:0000:0000:0000").shouldBeFalse()
    }

    @Test
    fun `should validate public IPv6 address`() {
        validateIpv6("2a00:1450:4001:806::200e").shouldBeTrue()
    }

    @Test
    fun `should validate MAC address`() {
        validateMac("ff:ff:ff:ff:ff:89").shouldBeTrue()
    }

    @Test
    fun `should validate MAC address with too many parts`() {
        validateMac("ff:ff:ff:ff:ff:ff:ff").shouldBeFalse()
    }

    @Test
    fun `should validate MAC address with non-hexadecimal numbers`() {
        validateMac("kk:kk:22:22:22:22").shouldBeFalse()
    }
}
