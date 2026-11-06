package app.yap.server.feature.characters.api

import app.yap.contract.admin.CharacterProfileDto
import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.CharacterId
import app.yap.server.feature.characters.model.CreateCharacterOutcome
import app.yap.server.feature.characters.model.UpdateCharacterOutcome
import app.yap.server.feature.characters.usecase.CreateCharacterUseCase
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.characters.usecase.GetCharactersUseCase
import app.yap.server.feature.characters.usecase.UpdateCharacterUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlin.uuid.Uuid

fun Route.characterRoutes(
    createCharacterUseCase: CreateCharacterUseCase,
    getCharacterUseCase: GetCharacterUseCase,
    getCharactersUseCase: GetCharactersUseCase,
    updateCharacterUseCase: UpdateCharacterUseCase,
) {
    route("/admin/characters") {
        get {
            call.respond(getCharactersUseCase().map { character -> character.toDto() })
        }
        post {
            when (val outcome = createCharacterUseCase(call.receive<CharacterProfileDto>().toModel())) {
                is CreateCharacterOutcome.Created -> call.respond(HttpStatusCode.Created, outcome.character.toDto())
                is CreateCharacterOutcome.Invalid -> call.respond(HttpStatusCode.BadRequest)
            }
        }
        get("/{characterId}") {
            val character = getCharacterUseCase(call.characterId())
            if (character == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(character.toDto())
            }
        }
        put("/{characterId}") {
            val character = Character(
                characterId = call.characterId(),
                profile = call.receive<CharacterProfileDto>().toModel(),
            )
            when (val outcome = updateCharacterUseCase(character)) {
                is UpdateCharacterOutcome.Invalid -> call.respond(HttpStatusCode.BadRequest)
                is UpdateCharacterOutcome.NotFound -> call.respond(HttpStatusCode.NotFound)
                is UpdateCharacterOutcome.Updated -> call.respond(outcome.character.toDto())
            }
        }
    }
}

private fun RoutingCall.characterId(): CharacterId = parameters["characterId"]
    ?.let { value -> Uuid.parseOrNull(value) }
    ?.let(::CharacterId)
    ?: throw NotFoundException()
