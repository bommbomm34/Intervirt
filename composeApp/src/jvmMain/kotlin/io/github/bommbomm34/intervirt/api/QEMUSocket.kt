package io.github.bommbomm34.intervirt.api

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

data class QEMUSocket (
    val socket: Socket,
    val reader: BufferedReader,
    val writer: PrintWriter
){
    companion object {
        fun open(port: Int): QEMUSocket {
            val socket = Socket("localhost", port)
            return QEMUSocket(
                socket,
                BufferedReader(InputStreamReader(socket.getInputStream())),
                PrintWriter(socket.getOutputStream(), true)
            )
        }
    }

    fun close() = socket.close()
}