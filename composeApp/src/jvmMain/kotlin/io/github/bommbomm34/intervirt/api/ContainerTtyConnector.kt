package io.github.bommbomm34.intervirt.api

import com.jediterm.terminal.TtyConnector
import io.github.bommbomm34.intervirt.data.RemoteContainerSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ContainerTtyConnector(
    private val session: RemoteContainerSession
) : TtyConnector {

    override fun read(buf: CharArray?, offset: Int, length: Int): Int {
        TODO("Not yet implemented")
    }

    override fun write(bytes: ByteArray?): Unit = runBlocking {
        bytes?.let { session.write(bytes).getOrThrow() }
    }

    override fun write(string: String?): Unit = runBlocking {
        string?.let { session.write(string.toByteArray()).getOrThrow() }
    }

    override fun isConnected(): Boolean = session.isConnected()

    override fun waitFor(): Int {
        TODO("Not yet implemented")
    }

    override fun ready(): Boolean = isConnected

    override fun getName(): String = session.id

    override fun close() = runBlocking {
        session.close()
    }
}