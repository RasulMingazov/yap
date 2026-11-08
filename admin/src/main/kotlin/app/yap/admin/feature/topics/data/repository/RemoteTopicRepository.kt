package app.yap.admin.feature.topics.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.callServer
import app.yap.admin.core.network.settings.ServerSettingsRepository
import app.yap.admin.core.network.toLoadResult
import app.yap.admin.feature.topics.data.api.TopicApi
import app.yap.admin.feature.topics.data.mapper.toDomain
import app.yap.admin.feature.topics.data.mapper.toDto
import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.Topic
import app.yap.admin.feature.topics.domain.TopicCalendar
import app.yap.admin.feature.topics.domain.TopicContent
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.repository.TopicRepository
import app.yap.contract.admin.TopicCalendarDto
import app.yap.contract.admin.TopicDto
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal class RemoteTopicRepository(
    private val serverSettingsRepository: ServerSettingsRepository,
    private val topicApi: TopicApi,
) : TopicRepository {

    override suspend fun calendar(): LoadResult<TopicCalendar> = serverSettingsRepository.callServer(
        failed = LoadResult.Failed,
        read = { response ->
            response.toLoadResult {
                body<TopicCalendarDto>()
                    .toDomain()
            }
        },
        send = { topicApi.calendar() },
    )

    override suspend fun create(content: TopicContent): SaveTopicResult = serverSettingsRepository.callServer(
        failed = SaveTopicResult.Failed,
        read = { response -> response.toSaveResult() },
        send = { topicApi.create(content.toDto()) },
    )

    override suspend fun find(topicId: TopicId): LoadTopicResult = serverSettingsRepository.callServer(
        failed = LoadTopicResult.Failed,
        read = { response -> response.toLoadTopicResult() },
        send = { topicApi.find(topicId.value) },
    )

    override suspend fun list(): LoadTopicsResult = serverSettingsRepository.callServer(
        failed = LoadTopicsResult.Failed,
        read = { response -> response.toLoadTopicsResult() },
        send = { topicApi.list() },
    )

    override suspend fun update(topic: Topic): SaveTopicResult = serverSettingsRepository.callServer(
        failed = SaveTopicResult.Failed,
        read = { response -> response.toSaveResult() },
        send = {
            topicApi.update(
                topicId = topic.topicId.value,
                content = topic.content.toDto(),
            )
        },
    )

    private suspend fun HttpResponse.toSaveResult(): SaveTopicResult = when (status) {
        HttpStatusCode.OK,
        HttpStatusCode.Created,
        -> SaveTopicResult.Saved(body<TopicDto>().toDomain())
        HttpStatusCode.BadRequest -> SaveTopicResult.Invalid
        else -> SaveTopicResult.Failed
    }

    private suspend fun HttpResponse.toLoadTopicResult(): LoadTopicResult = when (status) {
        HttpStatusCode.OK -> LoadTopicResult.Loaded(body<TopicDto>().toDomain())
        else -> LoadTopicResult.Failed
    }

    private suspend fun HttpResponse.toLoadTopicsResult(): LoadTopicsResult = when (status) {
        HttpStatusCode.OK -> LoadTopicsResult.Loaded(
            body<List<TopicDto>>()
                .map { topic -> topic.toDomain() },
        )
        else -> LoadTopicsResult.Failed
    }
}
