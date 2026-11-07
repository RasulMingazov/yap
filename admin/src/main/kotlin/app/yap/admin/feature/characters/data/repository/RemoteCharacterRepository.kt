package app.yap.admin.feature.characters.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.callServer
import app.yap.admin.core.network.toLoadResult
import app.yap.admin.core.network.url
import app.yap.admin.feature.characters.data.mapper.toDomain
import app.yap.admin.feature.characters.data.mapper.toDto
import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.CharacterProfile
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.domain.repository.CharacterRepository
import app.yap.admin.feature.server.domain.repository.ServerSettingsRepository
import app.yap.contract.admin.CharacterDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

internal class RemoteCharacterRepository(
    private val httpClient: HttpClient,
    private val serverSettingsRepository: ServerSettingsRepository,
) : CharacterRepository {

    override suspend fun create(profile: CharacterProfile): SaveCharacterResult = serverSettingsRepository.callServer(
        failed = SaveCharacterResult.Failed,
        read = { response -> response.toSaveResult() },
        send = { settings ->
            httpClient.post(settings.url(CHARACTERS_PATH)) {
                bearerAuth(settings.key)
                contentType(ContentType.Application.Json)
                setBody(profile.toDto())
            }
        },
    )

    override suspend fun find(characterId: CharacterId): LoadResult<Character> = serverSettingsRepository.callServer(
        failed = LoadResult.Failed,
        read = { response -> response.toLoadResult { body<CharacterDto>().toDomain() } },
        send = { settings ->
            httpClient.get(settings.url("$CHARACTERS_PATH/${characterId.value}")) { bearerAuth(settings.key) }
        },
    )

    override suspend fun list(): LoadResult<List<Character>> = serverSettingsRepository.callServer(
        failed = LoadResult.Failed,
        read = { response ->
            response.toLoadResult {
                body<List<CharacterDto>>()
                    .map { character -> character.toDomain() }
            }
        },
        send = { settings -> httpClient.get(settings.url(CHARACTERS_PATH)) { bearerAuth(settings.key) } },
    )

    override suspend fun update(character: Character): SaveCharacterResult = serverSettingsRepository.callServer(
        failed = SaveCharacterResult.Failed,
        read = { response -> response.toSaveResult() },
        send = { settings ->
            httpClient.put(settings.url("$CHARACTERS_PATH/${character.characterId.value}")) {
                bearerAuth(settings.key)
                contentType(ContentType.Application.Json)
                setBody(character.profile.toDto())
            }
        },
    )

    private suspend fun HttpResponse.toSaveResult(): SaveCharacterResult = when (status) {
        HttpStatusCode.OK,
        HttpStatusCode.Created,
        -> SaveCharacterResult.Saved(body<CharacterDto>().toDomain())
        HttpStatusCode.BadRequest -> SaveCharacterResult.Invalid
        else -> SaveCharacterResult.Failed
    }

    private companion object {

        const val CHARACTERS_PATH = "/admin/characters"
    }
}
