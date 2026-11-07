package app.yap.server

import app.yap.server.admin.ADMIN_AUTHENTICATION
import app.yap.server.admin.installAdminAuthentication
import app.yap.server.admin.serverRoutes
import app.yap.server.config.AppConfig
import app.yap.server.config.AppConfigLoader
import app.yap.server.feature.characters.api.characterRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.core.module.Module
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin

fun main() {
    val config = AppConfigLoader().load()
    embeddedServer(Netty, port = config.port) {
        serverModule(
            config = config,
            modules = appModules(config),
        )
    }
        .start(wait = true)
}

internal fun Application.serverModule(
    config: AppConfig,
    modules: List<Module>,
) {
    install(Koin) {
        modules(modules)
    }
    install(ContentNegotiation) {
        json()
    }
    installStatusHandling()
    installAdminAuthentication(adminApiKey = config.adminApiKey)

    val koin = getKoin()
    routing {
        get("/health") { call.respondText("ok") }
        authenticate(ADMIN_AUTHENTICATION) {
            serverRoutes(environment = config.environment)
            characterRoutes(
                createCharacterUseCase = koin.get(),
                getCharacterUseCase = koin.get(),
                getCharactersUseCase = koin.get(),
                updateCharacterUseCase = koin.get(),
            )
        }
    }
}
