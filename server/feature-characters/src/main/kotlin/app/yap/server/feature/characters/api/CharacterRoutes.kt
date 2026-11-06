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
import io.ktor.server.plugins.BadRequestException
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
            val created = createCharacterUseCase(call.receive<CharacterProfileDto>().toModel()).createdCharacter()
            call.respond(HttpStatusCode.Created, created.toDto())
        }
        get("/{characterId}") {
            val character = getCharacterUseCase(call.characterId()) ?: throw NotFoundException()
            call.respond(character.toDto())
        }
        put("/{characterId}") {
            val character = Character(
                characterId = call.characterId(),
                profile = call.receive<CharacterProfileDto>().toModel(),
            )
            call.respond(updateCharacterUseCase(character).updatedCharacter().toDto())
        }
    }
}

private fun CreateCharacterOutcome.createdCharacter(): Character = when (this) {
    is CreateCharacterOutcome.Created -> character
    is CreateCharacterOutcome.Invalid -> throw BadRequestException(EMPTY_FIELD)
}

private fun RoutingCall.characterId(): CharacterId = parameters["characterId"]
    ?.let { value -> Uuid.parseOrNull(value) }
    ?.let(::CharacterId)
    ?: throw NotFoundException()

private fun UpdateCharacterOutcome.updatedCharacter(): Character = when (this) {
    is UpdateCharacterOutcome.Invalid -> throw BadRequestException(EMPTY_FIELD)
    is UpdateCharacterOutcome.NotFound -> throw NotFoundException()
    is UpdateCharacterOutcome.Updated -> character
}

private const val EMPTY_FIELD = "Every field of the profile must be filled"
