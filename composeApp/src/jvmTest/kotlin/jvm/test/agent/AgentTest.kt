package jvm.test.agent

import io.github.bommbomm34.intervirt.api.AgentClient
import io.github.bommbomm34.intervirt.api.DeviceManager.addComputer
import io.github.bommbomm34.intervirt.api.DeviceManager.addSwitch
import io.github.bommbomm34.intervirt.api.DeviceManager.connectDevice
import io.github.bommbomm34.intervirt.api.DeviceManager.disconnectDevice
import io.github.bommbomm34.intervirt.api.DeviceManager.generateIPv6
import io.github.bommbomm34.intervirt.api.DeviceManager.removeDevice
import io.github.bommbomm34.intervirt.api.DeviceManager.runCommand
import io.github.bommbomm34.intervirt.api.DeviceManager.setIPv4
import io.github.bommbomm34.intervirt.api.DeviceManager.setIPv6
import io.github.bommbomm34.intervirt.api.DeviceManager.setInternetEnabled
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.data.ResultProgress
import io.github.bommbomm34.intervirt.logger
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class AgentTest {
    // DEBUGGING ONLY method which tests and debugs the Agent
    @Test
    fun debug(){
        runBlocking {
            // Create test computers
            println("----- START INTERVIRT AGENT DEBUGGING -----")
            val (computer1, computer2, computer3, computer4, computer5, switch) = createDevices()
            println("PASSED DEVICE CREATION TEST")
            removeDevice(computer5)
            println("PASSED DEVICE REMOVAL TEST")
            connectDevice(computer3, computer4)
            println("PASSED COMPUTER CONNECTION TEST")
            connectDevice(computer1, switch)
            connectDevice(computer2, switch)
            println("PASSED SWITCH CONNECTION TEST")
//            val testPing: suspend () -> List<ResultProgress<Unit>> = { runCommand(computer1, "ping -c 4 8.8.8.8").toList() }
//            setInternetEnabled(computer1, true)
//            val res1 = testPing()
//            if (!res1.any { it.message?.contains("0% packet loss") ?: false }) error("PING FAILED: \n${res1.joinToString { it.message ?: it.result?.exceptionOrNull()?.message ?: "" }}")
//            println { "INTERNET ENABLE TEST PASSED" }
//            setInternetEnabled(computer1, false)
//            val res2 = testPing()
//            if (!res2.any { it.message?.contains("Network is unreachable") ?: false }) error("PING MIGHT BE SUCCEEDED: \n${res1.joinToString { it.message ?: it.result?.exceptionOrNull()?.message ?: "" }}")
//            println { "INTERNET DISABLE TEST PASSED" }
            setIPv4(computer3, "192.168.9.8")
            setIPv4(computer4, "192.168.9.67")
            setIPv6(computer3, generateIPv6())
            setIPv6(computer4, generateIPv6())
            println("SET IP ADDRESSES PASSED")
            disconnectDevice(computer1, switch)
            println("PASSED SWITCH DISCONNECTION")
            AgentClient.wipe().collect { println("WIPE: ${it.message}") }
            println("PASSED WIPE")
            println("----- CONGRATULATIONS: ALL TESTS PASSED SUCCESSFULLY. -----")
        }
    }

    suspend fun createDevices(): SampleAgentData {
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
        return SampleAgentData(computer1, computer2, computer3, computer4, computer5, switch)
    }
}

data class SampleAgentData(
    val computer1: Device.Computer,
    val computer2: Device.Computer,
    val computer3: Device.Computer,
    val computer4: Device.Computer,
    val computer5: Device.Computer,
    val switch: Device.Switch,
)