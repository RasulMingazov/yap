package app.yap.admin.feature.levels.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.callServer
import app.yap.admin.core.network.settings.ServerSettingsRepository
import app.yap.admin.core.network.toLoadResult
import app.yap.admin.feature.levels.data.api.LevelApi
import app.yap.admin.feature.levels.data.mapper.toDomain
import app.yap.admin.feature.levels.data.mapper.toDto
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelEntry
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.repository.LevelRepository
import app.yap.contract.admin.LevelDto
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal class RemoteLevelRepository(
    private val levelApi: LevelApi,
    private val serverSettingsRepository: ServerSettingsRepository,
) : LevelRepository {

    override suspend fun list(): LoadResult<List<Level>> = serverSettingsRepository.callServer(
        failed = LoadResult.Failed,
        read = { response -> response.toLoadResult { toLevels() } },
        send = { levelApi.list() },
    )

    override suspend fun save(entries: List<LevelEntry>): SaveLevelsResult = serverSettingsRepository.callServer(
        failed = SaveLevelsResult.Failed,
        read = { response -> response.toSaveResult() },
        send = { levelApi.save(entries.map { entry -> entry.toDto() }) },
    )

    private suspend fun HttpResponse.toSaveResult(): SaveLevelsResult = when (status) {
        HttpStatusCode.OK -> SaveLevelsResult.Saved(toLevels())
        HttpStatusCode.BadRequest -> SaveLevelsResult.Invalid
        HttpStatusCode.Conflict -> SaveLevelsResult.ModelUnavailable
        else -> SaveLevelsResult.Failed
    }

    private suspend fun HttpResponse.toLevels(): List<Level> = body<List<LevelDto>>()
        .map { level -> level.toDomain() }
}
