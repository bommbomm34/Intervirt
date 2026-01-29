package io.github.bommbomm34.intervirt.api

import com.jediterm.terminal.TtyConnector
import io.github.bommbomm34.intervirt.data.RemoteContainerSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.PrintWriter
import kotlin.math.min

class ContainerTtyConnector(
    private val session: RemoteContainerSession
) : TtyConnector {

    override fun read(buf: CharArray, offset: Int, length: Int): Int {
        val bytes = session.read(length)
        val charArray = bytes.decodeToString().toCharArray()
        charArray.forEachIndexed { i, char ->
            buf[i + offset] = char
        }
        return min(charArray.size, buf.size)
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