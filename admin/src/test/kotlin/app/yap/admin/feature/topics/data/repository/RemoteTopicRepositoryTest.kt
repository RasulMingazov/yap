package app.yap.admin.feature.topics.data.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.network.adminHttpClient
import app.yap.admin.core.network.adminKtorfit
import app.yap.admin.core.network.settings.ServerSettings
import app.yap.admin.core.network.settings.StubServerSettings
import app.yap.admin.core.network.settings.StubServerSettingsRepository
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.topics.data.api.createTopicApi
import app.yap.admin.feature.topics.data.mapper.StubTopicDto
import app.yap.admin.feature.topics.domain.CalendarRow
import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.StubTopicCalendar
import app.yap.admin.feature.topics.domain.Todo
import app.yap.admin.feature.topics.domain.TopicGap
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.contract.admin.TopicGapDto
import app.yap.contract.admin.TopicRejectionDto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
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
    fun `GIVEN a day with a topic and an empty day WHEN loading the calendar THEN the server gets a GET and the calendar is loaded`() = runTest {
        val env = Environment(answer = answer(content = CALENDAR_JSON))

        val result = env.repository.calendar()

        val request = env.engine.requestHistory.single()
        val calendar = StubTopicCalendar.stubTopicCalendar(
            rows = listOf(
                CalendarRow(
                    date = StubTopic.RELEASE_DATE,
                    topic = StubTopicCalendar.stubCalendarTopic(),
                ),
                CalendarRow(
                    date = StubTopic.RELEASE_DATE.plusDays(1),
                    topic = null,
                ),
            ),
            todos = listOf(Todo.EmptyDay(date = StubTopic.RELEASE_DATE.plusDays(1))),
        )
        assertEquals(
            expected = listOf(LoadResult.Loaded(calendar), HttpMethod.Get, "${StubServerSettings.ADDRESS}/admin/topics/calendar"),
            actual = listOf(result, request.method, request.url.toString()),
        )
    }

    @Test
    fun `GIVEN the server fails WHEN loading the calendar THEN loading fails`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.InternalServerError))

        val result = env.repository.calendar()

        assertEquals(expected = LoadResult.Failed, actual = result)
    }

    @Test
    fun `GIVEN no saved server settings WHEN loading the calendar THEN loading fails and no request goes out`() = runTest {
        val env = Environment(answer = answer(content = CALENDAR_JSON), settings = null)

        val result = env.repository.calendar()

        assertEquals(expected = listOf(LoadResult.Failed, 0), actual = listOf(result, env.engine.requestHistory.size))
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
                ContentType.Application.Json,
            ),
            actual = listOf(
                result,
                request.method,
                request.url.toString(),
                request.headers[HttpHeaders.Authorization],
                Json.parseToJsonElement(request.body.toByteArray().decodeToString()),
                request.body.contentType,
            ),
        )
    }

    @Test
    fun `GIVEN a saved topic WHEN updating it THEN the server gets a PUT at its address`() = runTest {
        val env = Environment(answer = answer(content = TOPIC_JSON))

        val result = env.update()

        val request = env.engine.requestHistory.single()
        assertEquals(
            expected = listOf(
                SaveTopicResult.Saved(StubTopic.stubTopic()),
                HttpMethod.Put,
                "${StubServerSettings.ADDRESS}/admin/topics/${StubTopic.TOPIC_ID}",
                ContentType.Application.Json,
            ),
            actual = listOf(result, request.method, request.url.toString(), request.body.contentType),
        )
    }

    @Test
    fun `GIVEN the server rejects the draft WHEN updating a topic THEN the result holds the gaps`() = runTest {
        val env = Environment(
            answer = answer(
                content = Json.encodeToString(
                    TopicRejectionDto(gaps = listOf(TopicGapDto.PastDateDto, TopicGapDto.DateTakenDto)),
                ),
                status = HttpStatusCode.BadRequest,
            ),
        )

        val result = env.update()

        assertEquals(
            expected = SaveTopicResult.Invalid(gaps = listOf(TopicGap.PastDate, TopicGap.DateTaken)),
            actual = result,
        )
    }

    @Test
    fun `GIVEN the server rejects the draft with no body WHEN updating a topic THEN the result has no gaps`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.BadRequest))

        val result = env.update()

        assertEquals(expected = SaveTopicResult.Invalid(gaps = emptyList()), actual = result)
    }

    @Test
    fun `GIVEN the date is taken WHEN updating a topic THEN the date is taken`() = runTest {
        val env = Environment(answer = answer(content = "", status = HttpStatusCode.Conflict))

        val result = env.update()

        assertEquals(expected = SaveTopicResult.DateTaken, actual = result)
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
        val serverSettingsRepository = StubServerSettingsRepository(settings = settings)
        private val ktorfit = adminKtorfit(
            httpClient = adminHttpClient(serverSettingsRepository = serverSettingsRepository, engine = engine),
        )
        val repository = RemoteTopicRepository(
            serverSettingsRepository = serverSettingsRepository,
            topicApi = ktorfit.createTopicApi(),
        )

        suspend fun update(): SaveTopicResult = repository.update(
            content = StubTopic.stubContent(),
            topicId = TopicId(StubTopic.TOPIC_ID),
        )
    }

    private companion object {

        const val CALENDAR_JSON = """
            {
              "rows": [
                {
                  "date": "${StubTopicDto.RELEASE_DATE}",
                  "topic": {
                    "completeSteps": 1,
                    "ledBy": null,
                    "status": "DRAFT",
                    "steps": 1,
                    "title": "${StubTopic.TITLE}",
                    "topicId": "${StubTopic.TOPIC_ID}"
                  }
                },
                {
                  "date": "2025-10-25",
                  "topic": null
                }
              ],
              "todos": [{"type": "emptyDay", "date": "2025-10-25"}]
            }
        """
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
        const val TOPIC_JSON = """
            {
              "content": $CONTENT_JSON,
              "gaps": [],
              "status": "DRAFT",
              "topicId": "${StubTopic.TOPIC_ID}"
            }
        """

        fun answer(content: String, status: HttpStatusCode = HttpStatusCode.OK): MockRequestHandler = {
            respond(
                content = content,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
    }
}
