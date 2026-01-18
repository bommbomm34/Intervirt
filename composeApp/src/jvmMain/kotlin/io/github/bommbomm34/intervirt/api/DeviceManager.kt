package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.configuration
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.Image
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.data.connect
import io.github.bommbomm34.intervirt.data.toReadableImage
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import java.io.File
import kotlin.random.Random

object DeviceManager {
    suspend fun addComputer(name: String? = null, x: Int, y: Int, image: String): Device.Computer {
        val id = generateID("computer")
        val device = Device.Computer(
            id = id,
            image = image,
            name = name ?: id,
            x = x,
            y = y,
            ipv4 = generateIPv4(),
            ipv6 = generateIPv6(),
            internetEnabled = false,
            portForwardings = mutableMapOf()
        )
        logger.debug { "Adding device $device" }
        configuration.devices.add(device)
//        AgentClient.addContainer(device.id, device.ipv4, device.ipv6, false, image)
        return device
    }

    fun addSwitch(name: String? = null, x: Int, y: Int): Device.Switch {
        val id = generateID("switch")
        val device = Device.Switch(
            id = id,
            name = name ?: id,
            x = x,
            y = y
        )
        logger.debug { "Adding device $device" }
        configuration.devices.add(device)
        return device
    }

    suspend fun removeDevice(device: Device) {
        logger.debug { "Removing device $device" }
        configuration.devices.remove(device)
        configuration.connections.removeIf { it.containsDevice(device) }
//        AgentClient.removeContainer(device.id)
    }

    suspend fun connectDevice(device1: Device, device2: Device) {
        logger.debug { "Connecting device $device1 to $device2" }
        configuration.connections.add(device1 connect device2)
//        val device1ConnectedComputers = device1.getConnectedComputers(configuration.connections)
//        device2.getConnectedComputers(configuration.connections).forEach { computer1 ->
//            device1ConnectedComputers.forEach { computer2 -> AgentClient.connect(computer1.id, computer2.id) }
//        }
    }

    suspend fun disconnectDevice(device1: Device, device2: Device) {
        logger.debug { "Disconnecting device $device1 to $device2" }
        configuration.connections.removeIf { it == device1 connect device2 }
//        val device1ConnectedComputers = device1.getConnectedComputers(configuration.connections)
//        device2.getConnectedComputers(configuration.connections).forEach { computer1 ->
//            device1ConnectedComputers.forEach { computer2 -> AgentClient.disconnect(computer1.id, computer2.id) }
//        }
    }

    suspend fun setIPv4(device: Device.Computer, ipv4: String) {
        logger.debug { "Setting $ipv4 of $device" }
        device.ipv4 = ipv4
//        AgentClient.setIPv4(device.id, ipv4)
    }

    suspend fun setIPv6(device: Device.Computer, ipv6: String) {
        logger.debug { "Setting $ipv6 of $device" }
        device.ipv6 = ipv6
//        AgentClient.setIPv6(device.id, ipv6)
    }

    fun exportComputer(computer: Device.Computer): Flow<ResultProgress<File>> = flow {
        logger.debug { "Exporting disk of $computer" }
        emit(ResultProgress.result(AgentClient.getDisk(computer.id)))
    }

    fun runCommand(computer: Device.Computer, command: String) = AgentClient.runCommand(computer.id, command)

    fun setName(device: Device, name: String) {
        device.name = name
    }

    suspend fun setInternetEnabled(device: Device.Computer, enabled: Boolean) {
        logger.debug { "Set internet enabled of ${device.id} to $enabled" }
        device.internetEnabled = enabled
//        AgentClient.setInternetAccess(device.id, enabled)
    }

    suspend fun addPortForwarding(device: Device.Computer, internalPort: Int, externalPort: Int) {
        logger.debug { "Add port forwarding $internalPort:$externalPort for ${device.id}" }
        device.portForwardings[internalPort] = externalPort
//        AgentClient.addPortForwarding(device.id, internalPort, externalPort)
    }

    suspend fun removePortForwarding(externalPort: Int) {
        logger.debug { "Remove port forwarding of $externalPort" }
        configuration.devices.forEach { device ->
            if (device is Device.Computer)
                device.portForwardings.entries.removeIf { it.value == externalPort }
        }
//        AgentClient.removePortForwarding(externalPort)
    }

    // DEBUGGING ONLY method which tests and debugs the Agent
    suspend fun debug(){
        // Create test computers
        logger.debug { "----- START INTERVIRT AGENT DEBUGGING -----" }
        val computer1 = addComputer(
            name = "My First Computer",
            x = 120,
            y = 240,
            image = "debian/13"
        )
        val computer2 = addComputer(
            name = "My Second Computer",
            x = 120,
            y = 240,
            image = "debian/13"
        )
        val computer3 = addComputer(
            name = "My Third Computer",
            x = 120,
            y = 240,
            image = "debian/13"
        )
        val computer4 = addComputer(
            name = "My Fourth Computer",
            x = 120,
            y = 240,
            image = "debian/13"
        )
        val computer5 = addComputer(
            name = "My Fifth Computer",
            x = 120,
            y = 240,
            image = "debian/13"
        )
        val switch = addSwitch(
            name = "My Switch",
            x = 140,
            y = 270
        )
        logger.debug { "PASSED DEVICE CREATION TEST" }
        removeDevice(computer5)
        logger.debug { "PASSED DEVICE REMOVAL TEST" }
        connectDevice(computer3, computer4)
        logger.debug { "PASSED COMPUTER CONNECTION TEST" }
        connectDevice(computer1, switch)
        connectDevice(computer2, switch)
        logger.debug { "PASSED SWITCH CONNECTION TEST" }
        val testPing: suspend () -> List<ResultProgress<Unit>> = { runCommand(computer1, "ping -c 4 8.8.8.8").toList() }
        setInternetEnabled(computer1, true)
        val res1 = testPing()
        if (!res1.any { it.message?.contains("0% packet loss") ?: false }) error("PING FAILED: \n${res1.joinToString { it.message ?: it.result?.exceptionOrNull()?.message ?: "" }}")
        logger.debug { "INTERNET ENABLE TEST PASSED" }
        setInternetEnabled(computer1, false)
        val res2 = testPing()
        if (!res2.any { it.message?.contains("Network is unreachable") ?: false }) error("PING MIGHT BE SUCCEEDED: \n${res1.joinToString { it.message ?: it.result?.exceptionOrNull()?.message ?: "" }}")
        logger.debug { "INTERNET DISABLE TEST PASSED" }
        setIPv4(computer3, "192.168.9.8")
        setIPv4(computer4, "192.168.9.67")
        setIPv6(computer3, generateIPv6())
        setIPv6(computer4, generateIPv6())
        logger.debug { "SET IP ADDRESSES PASSED" }
        disconnectDevice(computer1, switch)
        logger.debug { "PASSED SWITCH DISCONNECTION" }
        AgentClient.wipe().collect { logger.debug { "WIPE: ${it.message}" } }
        logger.debug { "PASSED WIPE" }
        logger.debug { "----- CONGRATULATIONS: ALL TESTS PASSED SUCCESSFULLY. -----" }
    }

    private fun generateID(prefix: String): String {
        while (true) {
            val id = prefix + "-" + Random.nextInt(999999)
            if (configuration.devices.all { it.id != id }) return id
        }
    }

    private fun generateIPv4(): String {
        val rand = { Random.nextInt(256) }
        while (true) {
            val ipv4 = "192.168.${rand()}.${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.ipv4 != ipv4 else true }) return ipv4
        }
    }

    private fun generateIPv6(): String {
        val rand = { Random.nextInt(65536).toString(16) }
        val randFirst = { Random.nextInt(256).toString(16) }
        while (true) {
            val ipv6 = "fd${randFirst()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}:${rand()}"
            if (configuration.devices.all { if (it is Device.Computer) it.ipv6 != ipv6 else true }) return ipv6
        }
    }
}