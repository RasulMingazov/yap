package app.yap.server.feature.topics.api

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.persistence.StubTopicRepository
import app.yap.server.feature.topics.usecase.CreateTopicUseCase
import app.yap.server.feature.topics.usecase.GetTopicUseCase
import app.yap.server.feature.topics.usecase.GetTopicsUseCase
import app.yap.server.feature.topics.usecase.UpdateTopicUseCase
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

internal class TopicRoutesTest {

    @Test
    fun `GIVEN a topic WHEN listing the topics THEN the server answers with its JSON`() = testApplication {
        application { Environment().install(this) }

        val response = client.get("/admin/topics")

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement("[${topicJson()}]"),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a filled draft WHEN creating a topic THEN the server answers 201 with it`() = testApplication {
        application { Environment().install(this) }

        val response = postTopic(contentJson())

        assertEquals(
            expected = HttpStatusCode.Created to Json.parseToJsonElement(topicJson()),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a guest WHEN creating a topic THEN the server answers 201 with the guest`() = testApplication {
        val env = Environment()
        env.topicRepository.createCall.returns(
            StubTopic.stubTopic(
                content = StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = GUEST_DESCRIPTION)),
            ),
        )
        application { env.install(this) }

        val response = postTopic(contentJson(interlocutor = GUEST_JSON))

        assertEquals(
            expected = HttpStatusCode.Created to Json.parseToJsonElement(topicJson(interlocutor = GUEST_JSON)),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN no duration and no interlocutor WHEN creating a topic THEN the server answers 201`() = testApplication {
        application { Environment().install(this) }

        val response = postTopic(
            Json.encodeToString(StubTopicDto.stubContentDto(durationMinutes = null, interlocutor = null)),
        )

        assertEquals(expected = HttpStatusCode.Created, actual = response.status)
    }

    @Test
    fun `GIVEN a title of spaces WHEN creating a topic THEN the server answers 400`() = testApplication {
        application { Environment().install(this) }

        val response = postTopic(Json.encodeToString(StubTopicDto.stubContentDto(title = "  ")))

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `GIVEN no release date WHEN creating a topic THEN the server answers 400`() = testApplication {
        application { Environment().install(this) }

        val response = postTopic(Json.encodeToString(StubTopicDto.stubContentDto(releaseDate = null)))

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `GIVEN a duration of 0 minutes WHEN creating a topic THEN the server answers 400`() = testApplication {
        application { Environment().install(this) }

        val response = postTopic(Json.encodeToString(StubTopicDto.stubContentDto(durationMinutes = 0)))

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `GIVEN a character that does not exist WHEN creating a topic THEN the server answers 400`() = testApplication {
        val env = Environment()
        env.characterRepository.findCall.returns(null)
        application { env.install(this) }

        val response = postTopic(contentJson())

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `GIVEN a release date that is not a date WHEN creating a topic THEN the server answers 400`() = testApplication {
        application { Environment().install(this) }

        val response = postTopic(Json.encodeToString(StubTopicDto.stubContentDto(releaseDate = "24 October")))

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `GIVEN a topic WHEN reading it THEN the server answers with its JSON`() = testApplication {
        application { Environment().install(this) }

        val response = client.get("/admin/topics/${StubTopicDto.TOPIC_ID}")

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(topicJson()),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN an unknown topic WHEN reading it THEN the server answers 404`() = testApplication {
        val env = Environment()
        env.topicRepository.findCall.returns(null)
        application { env.install(this) }

        val response = client.get("/admin/topics/${StubTopicDto.TOPIC_ID}")

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    @Test
    fun `GIVEN an identifier that is not a UUID WHEN reading a topic THEN the server answers 404`() = testApplication {
        application { Environment().install(this) }

        val response = client.get("/admin/topics/at-the-cafe")

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    @Test
    fun `GIVEN a filled draft WHEN updating a topic THEN the server answers with it`() = testApplication {
        application { Environment().install(this) }

        val response = putTopic(contentJson())

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(topicJson()),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a title of spaces WHEN updating a topic THEN the server answers 400`() = testApplication {
        application { Environment().install(this) }

        val response = putTopic(Json.encodeToString(StubTopicDto.stubContentDto(title = "  ")))

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `GIVEN an unknown topic WHEN updating it THEN the server answers 404`() = testApplication {
        val env = Environment()
        env.topicRepository.updateCall.returns(false)
        application { env.install(this) }

        val response = putTopic(contentJson())

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val topicRepository = StubTopicRepository()

        fun install(application: Application) {
            val topicDraftRule = TopicDraftRule(
                getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
            )
            application.install(ContentNegotiation) {
                json()
            }
            application.routing {
                topicRoutes(
                    createTopicUseCase = CreateTopicUseCase(
                        topicDraftRule = topicDraftRule,
                        topicRepository = topicRepository,
                    ),
                    getTopicUseCase = GetTopicUseCase(topicRepository = topicRepository),
                    getTopicsUseCase = GetTopicsUseCase(topicRepository = topicRepository),
                    updateTopicUseCase = UpdateTopicUseCase(
                        topicDraftRule = topicDraftRule,
                        topicRepository = topicRepository,
                    ),
                )
            }
        }
    }

    private companion object {

        const val CAST_JSON = """{"type": "character", "characterId": "${StubTopicDto.CHARACTER_ID}"}"""
        const val GUEST_DESCRIPTION = "A tourist who asks for directions."
        const val GUEST_JSON = """{"type": "guest", "description": "$GUEST_DESCRIPTION"}"""

        fun contentJson(interlocutor: String = CAST_JSON): String = """
            {
              "description": "${StubTopic.DESCRIPTION}",
              "durationMinutes": ${StubTopic.DURATION_MINUTES},
              "interlocutor": $interlocutor,
              "openingLine": {"english": "${StubTopic.ENGLISH}", "translation": "${StubTopic.TRANSLATION}"},
              "releaseDate": "${StubTopicDto.RELEASE_DATE}",
              "role": "${StubTopic.ROLE}",
              "setting": "${StubTopic.SETTING}",
              "steps": [{"outcome": "${StubTopic.OUTCOME}", "passWhen": "${StubTopic.PASS_WHEN}", "task": "${StubTopic.TASK}"}],
              "title": "${StubTopic.TITLE}"
            }
        """

        fun topicJson(interlocutor: String = CAST_JSON): String =
            """{"content": ${contentJson(interlocutor)}, "topicId": "${StubTopicDto.TOPIC_ID}"}"""
    }
}

private suspend fun ApplicationTestBuilder.postTopic(body: String): HttpResponse = client.post("/admin/topics") {
    contentType(ContentType.Application.Json)
    setBody(body)
}

private suspend fun ApplicationTestBuilder.putTopic(body: String): HttpResponse =
    client.put("/admin/topics/${StubTopicDto.TOPIC_ID}") {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
