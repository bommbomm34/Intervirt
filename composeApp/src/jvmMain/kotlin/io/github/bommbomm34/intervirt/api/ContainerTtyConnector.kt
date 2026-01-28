package io.github.bommbomm34.intervirt.api

import com.jediterm.terminal.TtyConnector
import io.github.bommbomm34.intervirt.data.RemoteContainerSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContainerTtyConnector(
    private val session: RemoteContainerSession
) : TtyConnector {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun read(buf: CharArray?, offset: Int, length: Int): Int {
        TODO("Not yet implemented")
    }

    override fun write(bytes: ByteArray?) {
        scope.launch { bytes?.let { session.write(bytes) } }
    }

    override fun write(string: String?) {
        scope.launch { string?.let { session.write(string.toByteArray()) } }
    }

    override fun isConnected(): Boolean = session.isConnected()

    override fun waitFor(): Int {
        TODO("Not yet implemented")
    }

    override fun ready(): Boolean = isConnected

    override fun getName(): String = session.id

    override fun close(){
        scope.launch { session.close() }
    }
}