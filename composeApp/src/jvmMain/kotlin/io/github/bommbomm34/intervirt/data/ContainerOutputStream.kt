package io.github.bommbomm34.intervirt.data

import io.github.bommbomm34.intervirt.api.Executor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue

class ContainerOutputStream(
    val executor: Executor,
    val id: String
) : OutputStream() {
    val queue = LinkedBlockingQueue<Byte>()
    val scope = CoroutineScope(Dispatchers.IO)
    var writeJob = scope.launch {
        while (true) {
            if (queue.isNotEmpty()) {
                val array = queue.toByteArray()
                queue.clear() // Clear queue (we already have the array)
                executor.writePtyBytesOnContainer(
                    id = id,
                    bytes = array
                ) // Write bytes
            }
        }
    }

    override fun write(b: Int) {
        queue.add(b.toByte())
    }

    override fun close() {
        writeJob.cancel()
    }
}