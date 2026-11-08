package app.yap.admin.feature.characters.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.callServer
import app.yap.admin.core.network.settings.ServerSettingsRepository
import app.yap.admin.core.network.toLoadResult
import app.yap.admin.feature.characters.data.api.CharacterApi
import app.yap.admin.feature.characters.data.mapper.toDomain
import app.yap.admin.feature.characters.data.mapper.toDto
import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.CharacterProfile
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.domain.repository.CharacterRepository
import app.yap.contract.admin.CharacterDto
import app.yap.contract.admin.CharacterSummaryDto
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal class RemoteCharacterRepository(
    private val characterApi: CharacterApi,
    private val serverSettingsRepository: ServerSettingsRepository,
) : CharacterRepository {

    override suspend fun create(profile: CharacterProfile): SaveCharacterResult = serverSettingsRepository.callServer(
        failed = SaveCharacterResult.Failed,
        read = { response -> response.toSaveResult() },
        send = { characterApi.create(profile.toDto()) },
    )

    override suspend fun find(characterId: CharacterId): LoadResult<Character> = serverSettingsRepository.callServer(
        failed = LoadResult.Failed,
        read = { response -> response.toLoadResult { body<CharacterDto>().toDomain() } },
        send = { characterApi.find(characterId.value) },
    )

    override suspend fun list(): LoadResult<List<Character>> = serverSettingsRepository.callServer(
        failed = LoadResult.Failed,
        read = { response ->
            response.toLoadResult {
                body<List<CharacterSummaryDto>>()
                    .map { summary -> summary.toDomain() }
            }
        },
        send = { characterApi.list() },
    )

    override suspend fun update(character: Character): SaveCharacterResult = serverSettingsRepository.callServer(
        failed = SaveCharacterResult.Failed,
        read = { response -> response.toSaveResult() },
        send = {
            characterApi.update(
                characterId = character.characterId.value,
                profile = character.profile.toDto(),
            )
        },
    )

    private suspend fun HttpResponse.toSaveResult(): SaveCharacterResult = when (status) {
        HttpStatusCode.OK,
        HttpStatusCode.Created,
        -> SaveCharacterResult.Saved(body<CharacterDto>().toDomain())
        HttpStatusCode.BadRequest -> SaveCharacterResult.Invalid
        else -> SaveCharacterResult.Failed
    }
}
