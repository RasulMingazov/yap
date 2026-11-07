package app.yap.admin.feature.levels.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.callServer
import app.yap.admin.core.network.toLoadResult
import app.yap.admin.core.network.url
import app.yap.admin.feature.levels.data.mapper.toDomain
import app.yap.admin.feature.levels.data.mapper.toDto
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelEntry
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.repository.LevelRepository
import app.yap.admin.feature.server.domain.repository.ServerSettingsRepository
import app.yap.contract.admin.LevelDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

internal class RemoteLevelRepository(
    private val httpClient: HttpClient,
    private val serverSettingsRepository: ServerSettingsRepository,
) : LevelRepository {

    override suspend fun list(): LoadResult<List<Level>> = serverSettingsRepository.callServer(
        failed = LoadResult.Failed,
        read = { response -> response.toLoadResult { toLevels() } },
        send = { settings -> httpClient.get(settings.url(LEVELS_PATH)) { bearerAuth(settings.key) } },
    )

    override suspend fun save(entries: List<LevelEntry>): SaveLevelsResult = serverSettingsRepository.callServer(
        failed = SaveLevelsResult.Failed,
        read = { response -> response.toSaveResult() },
        send = { settings ->
            httpClient.put(settings.url(LEVELS_PATH)) {
                bearerAuth(settings.key)
                contentType(ContentType.Application.Json)
                setBody(entries.map { entry -> entry.toDto() })
            }
        },
    )

    private suspend fun HttpResponse.toSaveResult(): SaveLevelsResult = when (status) {
        HttpStatusCode.OK -> SaveLevelsResult.Saved(toLevels())
        HttpStatusCode.BadRequest -> SaveLevelsResult.Invalid
        else -> SaveLevelsResult.Failed
    }

    private suspend fun HttpResponse.toLevels(): List<Level> = body<List<LevelDto>>()
        .map { level -> level.toDomain() }

    private companion object {

        const val LEVELS_PATH = "/admin/levels"
    }
}
