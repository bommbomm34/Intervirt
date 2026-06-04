/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.Project
import java.util.*
import kotlin.random.Random

private val IPV4_REGEX = Regex("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$")
private val DIGITS_PATTERN = Regex("\\d{1,3}")
private val ID_CHECK_PATTERN = Regex("[^\\s/%]+")
private val MAC_REGEX = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")

fun Project.generateMac(): String {
    while (true) {
        val mac = randomMac()
        if (devices.all { if (it is Device.Computer) it.mac != mac else true }) return mac
    }
}

fun Project.generateIpv4(): String {
    while (true) {
        val ipv4 = randomIpv4()
        if (devices.all { if (it is Device.Computer) it.ipv4 != ipv4 else true }) return ipv4
    }
}

fun Project.generateIpv6(): String {
    while (true) {
        val ipv6 = randomIpv6()
        if (devices.all { if (it is Device.Computer) it.ipv6 != ipv6 else true }) return ipv6
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

fun randomIpv4(): String {
    fun rand() = Random.nextInt(2, 255)
    return "192.168.0.${rand()}"
}

fun randomIpv6(): String {
    fun rand() = Random.nextInt(65536)
        .toHex()
        .padZero(4)

    fun randFirst() = Random.nextInt(256)
        .toHex()
        .padZero(2)
    return "fd${randFirst()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}"
}

// Based on InetAddressValidator.isValidInet4Address() of Apache Commons
fun String.validateIpv4(): Boolean {
    val groups = IPV4_REGEX.matchEntire(this)
        ?.groups
        ?.drop(1)
        ?.filterNotNull() ?: return false
    for (ipSegmentResult in groups) {
        val ipSegment = ipSegmentResult.value
        if (ipSegment.isEmpty() || ipSegment.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        var iIpSegment: Int
        try {
            iIpSegment = ipSegment.toInt()
        } catch (_: NumberFormatException) {
            return false
        }
        if (iIpSegment > 255 || ipSegment.length > 1 && ipSegment.startsWith("0")) {
            return false
        }
    }
    return true
}

// Based on InetAddressValidator.isValidInet6Address() of Apache Commons
fun String.validateIpv6(): Boolean {
    var inet6Address = this
    // remove prefix size. This will appear after the zone id (if any)
    var parts: Array<String?> = inet6Address.split("/".toRegex()).toTypedArray()
    if (parts.size > 2) {
        return false // can only have one prefix specifier
    }
    if (parts.size == 2) {
        if (!DIGITS_PATTERN.matches(parts[1] ?: return false)) {
            return false // not a valid number
        }
        val bits = parts[1]!!.toInt() // cannot fail because of RE check
        if (bits !in 0..128) {
            return false // out of range
        }
    }
    // remove zone-id
    parts = parts[0]!!.split("%".toRegex()).toTypedArray()
    // The id syntax is implementation independent, but it presumably cannot allow:
    // whitespace, '/' or '%'
    if (parts.size > 2 || parts.size == 2 && !ID_CHECK_PATTERN.matches(parts[1] ?: return false)) {
        return false // invalid id
    }
    inet6Address = parts[0]!!
    val containsCompressedZeroes = inet6Address.contains("::")
    if (containsCompressedZeroes && inet6Address.indexOf("::") != inet6Address.lastIndexOf("::")) {
        return false
    }
    val startsWithCompressed = inet6Address.startsWith("::")
    val endsWithCompressed = inet6Address.endsWith("::")
    val endsWithSep = inet6Address.endsWith(":")
    if (inet6Address.startsWith(":") && !startsWithCompressed || endsWithSep && !endsWithCompressed) {
        return false
    }
    var octets: Array<String?> = inet6Address.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    if (containsCompressedZeroes) {
        val octetList: MutableList<String?> = ArrayList<String?>(Arrays.asList<String?>(*octets))
        if (endsWithCompressed) {
            // String.split() drops ending empty segments
            octetList.add("")
        } else if (startsWithCompressed && !octetList.isEmpty()) {
            octetList.removeAt(0)
        }
        octets = octetList.toTypedArray<String?>()
    }
    if (octets.size > 8) {
        return false
    }
    var validOctets = 0
    var emptyOctets = 0 // consecutive empty chunks
    for (index in octets.indices) {
        val octet = octets[index]
        if (octet?.isBlank() ?: true) {
            emptyOctets++
            if (emptyOctets > 1) {
                return false
            }
        } else {
            emptyOctets = 0
            // Is last chunk an IPv4 address?
            if (index == octets.size - 1 && octet.contains(".")) {
                if (!octet.validateIpv4()) {
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
    if (validOctets > 8 || validOctets < 8 && !containsCompressedZeroes) {
        return false
    }
    return true
}

fun String.validateMac(): Boolean = MAC_REGEX.matches(this)

fun String.padZero(len: Int) = padStart(len, '0')

fun Int.toHex(): String = toString(16)
