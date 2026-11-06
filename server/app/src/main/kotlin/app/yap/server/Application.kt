package app.yap.server

import app.yap.server.core.config.AppConfigLoader
import app.yap.server.core.database.openDatabase
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val config = AppConfigLoader().load()
    val database = openDatabase(config.database)
    embeddedServer(Netty, port = config.port) {
        monitor.subscribe(ApplicationStopped) { database.close() }
        serverModule()
    }
        .start(wait = true)
}

internal fun Application.serverModule() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/health") { call.respondText("ok") }
    }
}
