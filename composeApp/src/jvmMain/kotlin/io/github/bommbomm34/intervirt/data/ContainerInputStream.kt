package io.github.bommbomm34.intervirt.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.concurrent.LinkedBlockingQueue

class ContainerInputStream(val flow: Flow<ByteArray>) : InputStream() {
    private val queue = LinkedBlockingQueue<Byte>()
    private var finished = false
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun read() = if (finished) -1 else queue.take().toInt() and 0xFF
    override fun available() = queue.size

    init {
        scope.launch {
            flow.collect { array ->
                array.forEach { queue.put(it) }
            }
            finished = true
        }
    }
}