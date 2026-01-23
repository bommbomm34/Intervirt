package io.github.bommbomm34.intervirt

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSockets()
    configureSerialization()
    configureRouting()
}
