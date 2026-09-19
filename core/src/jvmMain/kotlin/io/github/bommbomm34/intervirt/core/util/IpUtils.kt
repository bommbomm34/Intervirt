/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import inet.ipaddr.ipv4.IPv4Address
import inet.ipaddr.ipv6.IPv6Address
import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.Project
import org.jetbrains.annotations.VisibleForTesting
import java.util.*
import kotlin.random.Random

private val IPV4_REGEX = Regex("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$")
private val DIGITS_PATTERN = Regex("\\d{1,3}")
private val ID_CHECK_PATTERN = Regex("[^\\s/%]+")
private val MAC_REGEX = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")

fun Project.generateMac(): String {
    while (true) {
        val mac = randomMac()
        if (devices.all { it !is Device.Computer || it.mac != mac }) return mac
    }
}

fun Project.generateIpv4(subnet: IPAddress): String {
    while (true) {
        val ipv4 = randomIpv4(subnet)
        if (devices.all { it !is Device.Computer || it.ipv4 != ipv4 }) return ipv4
    }
}

fun Project.generateIpv6(subnet: IPAddress): String {
    while (true) {
        val ipv6 = randomIpv6(subnet)
        if (devices.all { it !is Device.Computer || it.ipv6 != ipv6 }) return ipv6
    }
}

fun randomMac(): String {
    fun rand() = Random.nextInt(256)
        .toString(16)
        .padZero(2)

    fun randFirst(): String = ((Random.nextInt(256) and 0b1111_1100) or 0b0000_0010)
        .toString(16)
        .padZero(2)
    return "${randFirst()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}"
}

fun randomIpv4(subnet: IPAddress): String {
    require(subnet.isIPv4) { "Expected IPv4 subnet, but got $subnet" }
    val prefixLen = subnet.networkPrefixLength

    val result = subnet.bytes.copyOf()

    var bitIndex = prefixLen

    while (bitIndex < 32) {
        val byteIndex = bitIndex / 8
        val bitInByte = 7 - (bitIndex % 8)

        val randomBit = (Random.nextInt(2) shl bitInByte)
        result[byteIndex] = (result[byteIndex].toInt() or randomBit).toByte()

        bitIndex++
    }

    return IPv4Address(result).toCompressedString()
}

fun randomIpv6(subnet: IPAddress): String {
    require(subnet.isIPv6) { "Expected IPv6 subnet, but got $subnet" }
    val prefixLen = subnet.networkPrefixLength
    val hostBits = 128 - prefixLen

    val base = subnet.bytes.copyOf()

    val randomBytes = Random.nextBytesByBitCount(hostBits)

    val hostStartByte = prefixLen / 8
    val hostStartBitOffset = prefixLen % 8

    if (hostStartBitOffset == 0) {
        for (i in randomBytes.indices) {
            base[hostStartByte + i] = randomBytes[i]
        }
    } else {
        var bitIndex = prefixLen
        while (bitIndex < 128) {
            val byteIndex = bitIndex / 8
            val bitInByte = 7 - (bitIndex % 8)
            val bit = (randomBytes[(bitIndex - prefixLen) / 8].toInt() ushr (7 - ((bitIndex - prefixLen) % 8))) and 1
            base[byteIndex] = (base[byteIndex].toInt() and (1 shl bitInByte).inv() or (bit shl bitInByte)).toByte()
            bitIndex++
        }
    }

    return IPv6Address(base).toCompressedString()
}

// Based on InetAddressValidator.isValidInet4Address() of Apache Commons
fun validateIpv4(address: String): Boolean {
    val groups = IPV4_REGEX.matchEntire(address)
        ?.groups
        ?.drop(1)
        ?.filterNotNull() ?: return false
    for ((ipSegment) in groups) {
        if (ipSegment.isEmpty() || ipSegment.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        val iIpSegment = ipSegment.toIntOrNull() ?: return false
        if (iIpSegment > 255 || ipSegment.length > 1 && ipSegment.startsWith("0")) {
            return false
        }
    }
    return true
}

// Based on InetAddressValidator.isValidInet6Address() of Apache Commons
fun validateIpv6(address: String): Boolean {
    var inet6Address = address
    // remove prefix size. This will appear after the zone id (if any)
    var parts = inet6Address.split("/".toRegex())
    if (parts.size > 2) {
        return false // can only have one prefix specifier
    }
    if (parts.size == 2) {
        if (!DIGITS_PATTERN.matches(parts.getOrNull(1) ?: return false)) {
            return false // not a valid number
        }
        val bits = parts[1].toInt() // cannot fail because of RE check
        if (bits !in 0..128) {
            return false // out of range
        }
    }
    // remove zone-id
    parts = parts[0].split("%".toRegex())
    // The id syntax is implementation independent, but it presumably cannot allow:
    // whitespace, '/' or '%'
    if (parts.size > 2 || parts.size == 2 && !ID_CHECK_PATTERN.matches(parts.getOrNull(1) ?: return false)) {
        return false // invalid id
    }
    inet6Address = parts[0]
    val containsCompressedZeroes = "::" in inet6Address
    if (containsCompressedZeroes && inet6Address.indexOf("::") != inet6Address.lastIndexOf("::")) {
        return false
    }
    val startsWithCompressed = inet6Address.startsWith("::")
    val endsWithCompressed = inet6Address.endsWith("::")
    val endsWithSep = inet6Address.endsWith(":")
    if (inet6Address.startsWith(":") && !startsWithCompressed || endsWithSep && !endsWithCompressed) {
        return false
    }
    var octets = inet6Address.split(":".toRegex()).dropLastWhile { it.isEmpty() }
    if (containsCompressedZeroes) {
        val octetList = octets.toMutableList()
        if (endsWithCompressed) {
            // String.split() drops ending empty segments
            octetList += ""
        } else if (startsWithCompressed && octetList.isNotEmpty()) {
            octetList.removeAt(0)
        }
        octets = octetList
    }
    if (octets.size > 8) {
        return false
    }
    var validOctets = 0
    var emptyOctets = 0 // consecutive empty chunks
    for (index in octets.indices) {
        val octet = octets.getOrNull(index)
        if (octet?.isBlank() ?: true) {
            emptyOctets++
            if (emptyOctets > 1) {
                return false
            }
        } else {
            emptyOctets = 0
            // Is last chunk an IPv4 address?
            if (index == octets.size - 1 && "." in octet) {
                if (!validateIpv4(octet)) {
                    return false
                }
                validOctets += 2
                continue
            }
            if (octet.length > 4) {
                return false
            }
            var octetInt: Int
            try {
                octetInt = octet.toInt(16)
            } catch (_: NumberFormatException) {
                return false
            }
            if (octetInt !in 0..0xffff) {
                return false
            }
        }
        validOctets++
    }

    return !(validOctets > 8 || validOctets < 8 && !containsCompressedZeroes)
}

fun validateMac(address: String): Boolean = MAC_REGEX.matches(address)

infix fun String.isIPWithinSubnet(subnet: IPAddress): Boolean {
    val address = IPAddressString(this).getAddress() ?: throw IllegalArgumentException("Invalid IP address: $this")

    return address in subnet
}

fun String.padZero(len: Int) = padStart(len, '0')

@VisibleForTesting
internal fun Random.nextBytesByBitCount(bitCount: Int): ByteArray {
    val bytes = nextBytes(bitCount / 8)
    val diff = bitCount % 8
    if (diff == 0) return bytes
    val additionalByte = nextBits(diff).toByte()
    return bytes + additionalByte
}
