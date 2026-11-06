package app.yap.server

import app.yap.server.admin.ADMIN_AUTHENTICATION
import app.yap.server.admin.installAdminAuthentication
import app.yap.server.admin.serverRoutes
import app.yap.server.config.AppConfig
import app.yap.server.config.AppConfigLoader
import app.yap.server.core.database.openDatabase
import app.yap.server.feature.characters.api.characterRoutes
import app.yap.server.feature.characters.persistence.CharacterRepository
import app.yap.server.feature.characters.persistence.DefaultCharacterRepository
import app.yap.server.feature.characters.usecase.CreateCharacterUseCase
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.characters.usecase.GetCharactersUseCase
import app.yap.server.feature.characters.usecase.UpdateCharacterUseCase
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val config = AppConfigLoader().load()
    val database = openDatabase(config.database)
    runCatching {
        embeddedServer(Netty, port = config.port) {
            monitor.subscribe(ApplicationStopped) { database.close() }
            serverModule(
                characterRepository = DefaultCharacterRepository(database),
                config = config,
            )
        }
            .start(wait = true)
    }
        .onFailure { database.close() }
        .getOrThrow()
}

internal fun Application.serverModule(
    characterRepository: CharacterRepository,
    config: AppConfig,
) {
    install(ContentNegotiation) {
        json()
    }
    installStatusHandling()
    installAdminAuthentication(adminApiKey = config.adminApiKey)

    routing {
        get("/health") { call.respondText("ok") }
        authenticate(ADMIN_AUTHENTICATION) {
            serverRoutes(environment = config.environment)
            characterRoutes(
                createCharacterUseCase = CreateCharacterUseCase(characterRepository = characterRepository),
                getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
                getCharactersUseCase = GetCharactersUseCase(characterRepository = characterRepository),
                updateCharacterUseCase = UpdateCharacterUseCase(characterRepository = characterRepository),
            )
        }
    }
}
