package app.yap.admin.feature.topics.data.repository

import app.yap.admin.core.network.callServer
import app.yap.admin.core.network.settings.ServerSettingsRepository
import app.yap.admin.feature.topics.data.mapper.toDomain
import app.yap.admin.feature.topics.data.mapper.toDto
import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.Topic
import app.yap.admin.feature.topics.domain.TopicContent
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.repository.TopicRepository
import app.yap.contract.admin.TopicDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

internal class RemoteTopicRepository(
    private val httpClient: HttpClient,
    private val serverSettingsRepository: ServerSettingsRepository,
) : TopicRepository {

    override suspend fun create(content: TopicContent): SaveTopicResult = serverSettingsRepository.callServer(
        failed = SaveTopicResult.Failed,
        read = { response -> response.toSaveResult() },
        send = {
            httpClient.post(TOPICS_PATH) {
                contentType(ContentType.Application.Json)
                setBody(content.toDto())
            }
        },
    )

    override suspend fun find(topicId: TopicId): LoadTopicResult = serverSettingsRepository.callServer(
        failed = LoadTopicResult.Failed,
        read = { response -> response.toLoadTopicResult() },
        send = { httpClient.get("$TOPICS_PATH/${topicId.value}") },
    )

    override suspend fun list(): LoadTopicsResult = serverSettingsRepository.callServer(
        failed = LoadTopicsResult.Failed,
        read = { response -> response.toLoadTopicsResult() },
        send = { httpClient.get(TOPICS_PATH) },
    )

    override suspend fun update(topic: Topic): SaveTopicResult = serverSettingsRepository.callServer(
        failed = SaveTopicResult.Failed,
        read = { response -> response.toSaveResult() },
        send = {
            httpClient.put("$TOPICS_PATH/${topic.topicId.value}") {
                contentType(ContentType.Application.Json)
                setBody(topic.content.toDto())
            }
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

    private companion object {

        const val TOPICS_PATH = "/admin/topics"
    }
}
