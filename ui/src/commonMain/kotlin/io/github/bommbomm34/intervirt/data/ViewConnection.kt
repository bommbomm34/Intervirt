/*
 * Copyright (c) 2026. Intervirt Contributors
 * Licensed under the GNU General Public License 3.
 */

package io.github.bommbomm34.intervirt.data

data class ViewConnection(
    val device1: ViewDevice,
    val device2: ViewDevice,
) {
    fun containsDevice(device: ViewDevice) = device1.id == device.id || device2.id == device.id
}