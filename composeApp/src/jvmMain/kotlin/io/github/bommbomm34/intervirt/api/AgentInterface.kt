package io.github.bommbomm34.intervirt.api

import io.github.bommbomm34.intervirt.client
import io.github.bommbomm34.intervirt.data.AddContainerBody
import io.github.bommbomm34.intervirt.data.ConnectBody
import io.github.bommbomm34.intervirt.data.ContainerInfo
import io.github.bommbomm34.intervirt.data.FileManagement
import io.github.bommbomm34.intervirt.data.ForwardPortBody
import io.github.bommbomm34.intervirt.data.IDBody
import io.github.bommbomm34.intervirt.data.IDWithNewIPBody
import io.github.bommbomm34.intervirt.data.ResponseBody
import io.github.bommbomm34.intervirt.data.SetInternetAccessBody
import io.github.bommbomm34.intervirt.data.VersionResponseBody
import io.github.bommbomm34.intervirt.exceptions.DeviceNotFoundException
import io.github.bommbomm34.intervirt.exceptions.UndefinedError
import io.github.bommbomm34.intervirt.result
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import java.io.File

class AgentInterface(val fileManagement: FileManagement) {

    suspend fun addContainer(id: String, initialIPv4: String, initialIPv6: String, internet: Boolean): Result<Unit> {
        val response = put("container", AddContainerBody(id, initialIPv4, initialIPv6, internet))
        return if (response.status == HttpStatusCode.OK) Result.success(Unit)
        else Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun removeContainer(id: String): Result<Unit> {
        val response = delete("container?id=$id")
        return if (response.status == HttpStatusCode.OK) Result.success(Unit)
        else Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun getCommit(id: String): Result<File> {
        return fileManagement.downloadFile(
            "http://localhost:55436/commit",
            "img-$id-${System.currentTimeMillis()}.tar.gz"
        ).first { it.result != null }.result!!
    }

    suspend fun setIPv4(id: String, newIP: String): Result<Unit> {
        val response = post("ipv4", IDWithNewIPBody(id, newIP))
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun setIPv6(id: String, newIP: String): Result<Unit> {
        val response = post("ipv6", IDWithNewIPBody(id, newIP))
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun connect(id1: String, id2: String): Result<Unit> {
        val response = post("connect", ConnectBody(id1, id2))
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun disconnect(id: String): Result<Unit> {
        val response = post("disconnect", IDBody(id))
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun setInternetAccess(id: String, enabled: Boolean): Result<Unit> {
        val response = post("setInternetAccess", SetInternetAccessBody(id, enabled))
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun forwardPort(id: String, internalPort: Int, externalPort: Int): Result<Unit> {
        val response = put("forwardPort", ForwardPortBody(id, internalPort, externalPort))
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun removeForwardPort(externalPort: Int): Result<Unit> {
        val response = delete("forwardPort?externalPort=$externalPort")
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun wipe(): Result<Unit> {
        val response = post("wipe")
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun update(): Result<Unit> {
        val response = post("update")
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun shutdown(): Result<Unit> {
        val response = post("shutdown")
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun reboot(): Result<Unit> {
        val response = post("reboot")
        return if (response.status == HttpStatusCode.OK) Result.success(Unit) else
            Result.failure(response.body<ResponseBody>().exception())
    }

    suspend fun listContainerInfos(): Result<List<ContainerInfo>> {
        val response = get("containerInfos")
        return when (response.status) {
            HttpStatusCode.OK -> Result.success(response.body())
            HttpStatusCode.NotFound -> DeviceNotFoundException().result()
            else -> response.body<ResponseBody>().exception().result()
        }
    }

    suspend fun getVersion(): String = get("version").body<VersionResponseBody>().version

    private suspend inline fun <reified T> put(endpoint: String, body: T): HttpResponse {
        return client.put("http://localhost:55436/$endpoint") {
            setBody(body)
        }
    }

    private suspend inline fun <reified T> post(endpoint: String, body: T): HttpResponse {
        return client.post("http://localhost:55436/$endpoint") {
            setBody(body)
        }
    }

    private suspend fun post(endpoint: String): HttpResponse {
        return client.post("http://localhost:55436/$endpoint")
    }

    private suspend fun get(url: String): HttpResponse {
        return client.get("http://localhost:55436/$url")
    }

    private suspend fun delete(url: String): HttpResponse {
        return client.delete("http://localhost:55436/$url")
    }
}