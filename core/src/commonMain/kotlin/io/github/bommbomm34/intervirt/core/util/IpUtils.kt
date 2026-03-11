/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.core.util

import io.github.bommbomm34.intervirt.core.data.Device
import io.github.bommbomm34.intervirt.core.data.Project
import kotlin.random.Random

suspend fun Project.generateMac(): String {
    devices.withLock {
        while (true) {
            val mac = randomMac()
            if (all { if (it is Device.Computer) it.mac.get() != mac else true }) return mac
        }
    }
}

suspend fun Project.generateIpv4(): String {
    devices.withLock {
        while (true) {
            val ipv4 = randomIpv4()
            if (all { if (it is Device.Computer) it.ipv4.get() != ipv4 else true }) return ipv4
        }
    }
}

suspend fun Project.generateIpv6(): String {
    devices.withLock {
        while (true) {
            val ipv6 = randomIpv6()
            if (all { if (it is Device.Computer) it.ipv6.get() != ipv6 else true }) return ipv6
        }
    }
}

fun randomMac(): String {
    fun rand() = Random.nextInt(256).toString(16)
    return "${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}"
}

fun randomIpv4(): String {
    fun rand() = Random.nextInt(256)
    return "192.168.${rand()}.${rand()}"
}
fun randomIpv6(): String {
    fun rand() = Random.nextInt(65536).toString(16)
    fun randFirst() = Random.nextInt(256).toString(16)
    return "fd${randFirst()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}"
}