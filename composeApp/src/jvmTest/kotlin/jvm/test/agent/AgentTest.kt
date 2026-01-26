package jvm.test.agent

import io.github.bommbomm34.intervirt.api.AgentClient
import io.github.bommbomm34.intervirt.api.DeviceManager
import io.github.bommbomm34.intervirt.data.Device
import io.github.bommbomm34.intervirt.mainModule
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.fold
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull



class AgentTest : KoinTest {
    val logger = KotlinLogging.logger { }

    // DEBUGGING ONLY method which tests and debugs the Agent
    @Test
    fun debug() {
        runBlocking {
            // Create test computers
            logger.info { "----- START INTERVIRT AGENT TEST -----" }
            startKoin {
                modules(mainModule)
            }
            val deviceManager = get<DeviceManager>()
            val agentClient = get<AgentClient>()

            assertResult(agentClient.getVersion(), "VERSION TEST")
            val (computer1, computer2, computer3, computer4, computer5, switch) = assertResult(createDevices(deviceManager), "DEVICE CREATION TEST")
            assertResult(deviceManager.removeDevice(computer5), "DEVICE REMOVAL TEST")
            assertResult(deviceManager.connectDevice(computer3, computer4), "COMPUTER CONNECTION TEST")
            assertResult(deviceManager.connectDevice(computer1, switch), "SWITCH CONNECTION TEST")
            assertResult(deviceManager.connectDevice(computer2, switch), "SWITCH CONNECTION TEST")
//            val testPing: suspend () -> List<ResultProgress<Unit>> = { runCommand(computer1, "ping -c 4 8.8.8.8").toList() }
//            setInternetEnabled(computer1, true)
//            val res1 = testPing()
//            if (!res1.any { it.message?.contains("0% packet loss") ?: false }) error("PING FAILED: \n${res1.joinToString { it.message ?: it.result?.exceptionOrNull()?.message ?: "" }}")
//            println { "INTERNET ENABLE TEST PASSED" }
//            setInternetEnabled(computer1, false)
//            val res2 = testPing()
//            if (!res2.any { it.message?.contains("Network is unreachable") ?: false }) error("PING MIGHT BE SUCCEEDED: \n${res1.joinToString { it.message ?: it.result?.exceptionOrNull()?.message ?: "" }}")
//            println { "INTERNET DISABLE TEST PASSED" }
            assertResult(deviceManager.setIpv4(computer3, "192.168.9.8"), "SET IPV4 ADDRESS TEST")
            assertResult(deviceManager.setIpv4(computer4, "192.168.9.67"), "SET IPV4 ADDRESS TEST")
            assertResult(deviceManager.setIpv6(computer3, deviceManager.generateIpv6()), "SET IPV6 ADDRESS TEST")
            assertResult(deviceManager.setIpv6(computer4, deviceManager.generateIpv6()), "SET IPV6 ADDRESS TEST")
            assertResult(deviceManager.disconnectDevice(computer1, switch), "SWITCH DISCONNECTION TEST")
            agentClient.wipe().collect { progress ->
                progress.result?.onFailure { throw AssertionError("FAILED WIPE: $it") }
                println("WIPE: ${progress.message}")
            }
            logger.info { "----- CONGRATULATIONS: ALL TESTS PASSED SUCCESSFULLY. -----" }
        }
    }

    suspend fun createDevices(deviceManager: DeviceManager): Result<SampleAgentData> {
        val computers = (1..5).map { num ->
            val res = deviceManager.addComputer(
                name = "My Computer no. $num",
                x = 120,
                y = 240,
                image = "debian/13"
            )
            res.getOrElse { return Result.failure(it) }
        }
        val switch = deviceManager.addSwitch(
            name = "My Switch",
            x = 140,
            y = 270
        )
        return Result.success(
            SampleAgentData(
                computer1 = computers[0],
                computer2 = computers[1],
                computer3 = computers[2],
                computer4 = computers[3],
                computer5 = computers[4],
                switch = switch
            )
        )
    }

    fun <T> assertResult(result: Result<T>, test: String = ""): T {
        result.fold(
            onSuccess = {
                logger.info { "PASSED $test: $it" }
                return result.getOrNull()!!
            },
            onFailure = {
                throw AssertionError("FAILED $test: $it")
            }
        )
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