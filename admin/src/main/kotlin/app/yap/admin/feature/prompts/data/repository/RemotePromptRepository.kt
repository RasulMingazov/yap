package app.yap.admin.feature.prompts.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.callServer
import app.yap.admin.core.network.settings.ServerSettingsRepository
import app.yap.admin.core.network.toLoadResult
import app.yap.admin.feature.prompts.data.api.PromptApi
import app.yap.admin.feature.prompts.data.mapper.toDomain
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.RestorePromptResult
import app.yap.admin.feature.prompts.domain.SavePromptResult
import app.yap.admin.feature.prompts.domain.repository.PromptRepository
import app.yap.contract.admin.PromptDto
import app.yap.contract.admin.PromptSummaryDto
import app.yap.contract.admin.PromptTextDto
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal class RemotePromptRepository(
    private val promptApi: PromptApi,
    private val serverSettingsRepository: ServerSettingsRepository,
) : PromptRepository {

    override suspend fun find(promptKey: PromptKey): LoadResult<Prompt> = serverSettingsRepository.callServer(
        failed = LoadResult.Failed,
        read = { response -> response.toLoadResult { toPrompt() } },
        send = { promptApi.find(promptKey.name) },
    )

    override suspend fun list(): LoadResult<List<PromptSummary>> = serverSettingsRepository.callServer(
        failed = LoadResult.Failed,
        read = { response ->
            response.toLoadResult {
                body<List<PromptSummaryDto>>()
                    .map { prompt -> prompt.toDomain() }
            }
        },
        send = { promptApi.list() },
    )

    override suspend fun restore(
        promptKey: PromptKey,
        number: Int,
    ): RestorePromptResult = serverSettingsRepository.callServer(
        failed = RestorePromptResult.Failed,
        read = { response -> response.toRestoreResult() },
        send = {
            promptApi.restore(
                promptKey = promptKey.name,
                number = number,
            )
        },
    )

    override suspend fun save(
        promptKey: PromptKey,
        text: String,
    ): SavePromptResult = serverSettingsRepository.callServer(
        failed = SavePromptResult.Failed,
        read = { response -> response.toSaveResult() },
        send = {
            promptApi.save(
                promptKey = promptKey.name,
                text = PromptTextDto(text = text),
            )
        },
    )

    private suspend fun HttpResponse.toRestoreResult(): RestorePromptResult =
        if (status == HttpStatusCode.OK) RestorePromptResult.Restored(toPrompt()) else RestorePromptResult.Failed

    private suspend fun HttpResponse.toSaveResult(): SavePromptResult = when (status) {
        HttpStatusCode.Created -> SavePromptResult.Saved(toPrompt())
        HttpStatusCode.BadRequest -> SavePromptResult.EmptyText
        else -> SavePromptResult.Failed
    }

    private suspend fun HttpResponse.toPrompt(): Prompt = body<PromptDto>().toDomain()
}
