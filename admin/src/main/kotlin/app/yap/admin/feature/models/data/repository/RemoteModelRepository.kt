package app.yap.admin.feature.models.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.callServer
import app.yap.admin.core.network.settings.ServerSettingsRepository
import app.yap.admin.core.network.toLoadResult
import app.yap.admin.feature.models.data.api.ModelApi
import app.yap.admin.feature.models.data.mapper.toDomain
import app.yap.admin.feature.models.data.mapper.toDto
import app.yap.admin.feature.models.domain.Models
import app.yap.admin.feature.models.domain.SaveModelsResult
import app.yap.admin.feature.models.domain.TaskModel
import app.yap.admin.feature.models.domain.repository.ModelRepository
import app.yap.contract.admin.ModelsDto
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal class RemoteModelRepository(
    private val modelApi: ModelApi,
    private val serverSettingsRepository: ServerSettingsRepository,
) : ModelRepository {

    override suspend fun list(): LoadResult<Models> = serverSettingsRepository.callServer(
        failed = LoadResult.Failed,
        read = { response -> response.toLoadResult { toModels() } },
        send = { modelApi.list() },
    )

    override suspend fun save(tasks: List<TaskModel>): SaveModelsResult = serverSettingsRepository.callServer(
        failed = SaveModelsResult.Failed,
        read = { response -> response.toSaveResult() },
        send = { modelApi.save(tasks.map { task -> task.toDto() }) },
    )

    private suspend fun HttpResponse.toSaveResult(): SaveModelsResult = when (status) {
        HttpStatusCode.OK -> SaveModelsResult.Saved(toModels())
        HttpStatusCode.BadRequest -> SaveModelsResult.Invalid
        HttpStatusCode.Conflict -> SaveModelsResult.CatalogChanged
        else -> SaveModelsResult.Failed
    }

    private suspend fun HttpResponse.toModels(): Models = body<ModelsDto>().toDomain()
}
