package app.yap.admin.feature.topics.data.repository

import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.server.domain.ServerSettings
import app.yap.admin.feature.server.domain.StubServerSettings
import app.yap.admin.feature.server.domain.repository.StubServerSettingsRepository
import app.yap.admin.feature.topics.data.mapper.StubTopicDto
import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.StubTopic
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

internal class RemoteTopicRepositoryTest {

    @Test
    fun `GIVEN the server lists topics WHEN loading them THEN they are loaded`() = runTest {
        val env = Environment(answer = answer(content = "[$TOPIC_JSON]"))

        val result = env.repository.list()

        assertEquals(expected = LoadTopicsResult.Loaded(listOf(StubTopic.stubTopic())), actual = result)
    }

    @Test
    fun `GIVEN no saved server settings WHEN loading the topics THEN loading fails`() = runTest {
        val env = Environment(answer = answer(content = "[]"), settings = null)

        val result = env.repository.list()

        assertEquals(expected = LoadTopicsResult.Failed to 0, actual = result to env.engine.requestHistory.size)
    }

    @Test
    fun `GIVEN a saved topic WHEN loading it THEN the server gets a GET at its address`() = runTest {
        val env = Environment(answer = answer(content = TOPIC_JSON))

        val result = env.repository.find(StubTopic.stubTopic().topicId)

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                LoadTopicResult.Loaded(StubTopic.stubTopic()),
                HttpMethod.Get,
                "${StubServerSettings.ADDRESS}/admin/topics/${StubTopic.TOPIC_ID}",
            ),
            actual = listOf(result, request.method, request.url.toString()),
        )
    }

    @Test
    fun `GIVEN an unknown topic WHEN loading it THEN loading fails`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.NotFound))

        val result = env.repository.find(StubTopic.stubTopic().topicId)

        assertEquals(expected = LoadTopicResult.Failed, actual = result)
    }

    @Test
    fun `GIVEN a new topic WHEN creating it THEN the server gets its content with the key`() = runTest {
        val env = Environment(answer = answer(content = TOPIC_JSON, status = HttpStatusCode.Created))

        val result = env.repository.create(StubTopic.stubContent())

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                SaveTopicResult.Saved(StubTopic.stubTopic()),
                HttpMethod.Post,
                "${StubServerSettings.ADDRESS}/admin/topics",
                "Bearer ${StubServerSettings.KEY}",
                Json.parseToJsonElement(CONTENT_JSON),
            ),
            actual = listOf(
                result,
                request.method,
                request.url.toString(),
                request.headers[HttpHeaders.Authorization],
                Json.parseToJsonElement(request.body.toByteArray().decodeToString()),
            ),
        )
    }

    @Test
    fun `GIVEN a saved topic WHEN updating it THEN the server gets a PUT at its address`() = runTest {
        val env = Environment(answer = answer(content = TOPIC_JSON))

        val result = env.repository.update(StubTopic.stubTopic())

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                SaveTopicResult.Saved(StubTopic.stubTopic()),
                HttpMethod.Put,
                "${StubServerSettings.ADDRESS}/admin/topics/${StubTopic.TOPIC_ID}",
            ),
            actual = listOf(result, request.method, request.url.toString()),
        )
    }

    @Test
    fun `GIVEN the server rejects the draft WHEN updating a topic THEN the draft is invalid`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.BadRequest))

        val result = env.repository.update(StubTopic.stubTopic())

        assertEquals(expected = SaveTopicResult.Invalid, actual = result)
    }

    @Test
    fun `GIVEN the server cannot be reached WHEN creating a topic THEN the save fails`() = runTest {
        val env = Environment(answer = { throw IOException("Connection refused") })

        val result = env.repository.create(StubTopic.stubContent())

        assertEquals(expected = SaveTopicResult.Failed, actual = result)
    }

    private class Environment(
        answer: MockRequestHandler,
        settings: ServerSettings? = StubServerSettings.stubServerSettings(),
    ) {

        val engine = MockEngine(answer)
        val repository = RemoteTopicRepository(
            httpClient = adminHttpClient(engine),
            serverSettingsRepository = StubServerSettingsRepository(settings = settings),
        )
    }

    private companion object {

        const val CONTENT_JSON = """
            {
              "description": "${StubTopic.DESCRIPTION}",
              "durationMinutes": ${StubTopic.DURATION_MINUTES},
              "interlocutor": {"type": "character", "characterId": "${StubCharacter.CHARACTER_ID}"},
              "openingLine": {"english": "${StubTopic.ENGLISH}", "translation": "${StubTopic.TRANSLATION}"},
              "releaseDate": "${StubTopicDto.RELEASE_DATE}",
              "role": "${StubTopic.ROLE}",
              "setting": "${StubTopic.SETTING}",
              "steps": [{"outcome": "${StubTopic.OUTCOME}", "passWhen": "${StubTopic.PASS_WHEN}", "task": "${StubTopic.TASK}"}],
              "title": "${StubTopic.TITLE}"
            }
        """
        const val TOPIC_JSON = """{"content": $CONTENT_JSON, "topicId": "${StubTopic.TOPIC_ID}"}"""

        fun answer(content: String, status: HttpStatusCode = HttpStatusCode.OK): MockRequestHandler = {
            respond(
                content = content,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
    }
}
