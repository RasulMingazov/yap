package app.yap.admin.feature.topics.data.repository

import app.yap.admin.core.common.runSuspendCatching
import app.yap.admin.feature.server.domain.ServerSettings
import app.yap.admin.feature.server.domain.repository.ServerSettingsRepository
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
import io.ktor.client.request.bearerAuth
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

    override suspend fun create(content: TopicContent): SaveTopicResult = request(
        failed = SaveTopicResult.Failed,
        read = { response -> response.toSaveResult() },
        send = { settings ->
            httpClient.post(settings.url(TOPICS_PATH)) {
                bearerAuth(settings.key)
                contentType(ContentType.Application.Json)
                setBody(content.toDto())
            }
        },
    )

    override suspend fun find(topicId: TopicId): LoadTopicResult = request(
        failed = LoadTopicResult.Failed,
        read = { response -> response.toLoadTopicResult() },
        send = { settings ->
            httpClient.get(settings.url("$TOPICS_PATH/${topicId.value}")) { bearerAuth(settings.key) }
        },
    )

    override suspend fun list(): LoadTopicsResult = request(
        failed = LoadTopicsResult.Failed,
        read = { response -> response.toLoadTopicsResult() },
        send = { settings -> httpClient.get(settings.url(TOPICS_PATH)) { bearerAuth(settings.key) } },
    )

    override suspend fun update(topic: Topic): SaveTopicResult = request(
        failed = SaveTopicResult.Failed,
        read = { response -> response.toSaveResult() },
        send = { settings ->
            httpClient.put(settings.url("$TOPICS_PATH/${topic.topicId.value}")) {
                bearerAuth(settings.key)
                contentType(ContentType.Application.Json)
                setBody(topic.content.toDto())
            }
        },
    )

    private suspend fun <T> request(
        failed: T,
        read: suspend (HttpResponse) -> T,
        send: suspend (ServerSettings) -> HttpResponse,
    ): T {
        val settings = serverSettingsRepository.get() ?: return failed
        return runSuspendCatching { read(send(settings)) }
            .getOrElse { failed }
    }

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

    private fun ServerSettings.url(path: String): String = "${address.trimEnd('/')}$path"

    private companion object {

        const val TOPICS_PATH = "/admin/topics"
    }
}
