package app.yap.server.feature.topics.api

import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.characters.usecase.GetCharactersUseCase
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicCalendarRule
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicPublishRule
import app.yap.server.feature.topics.model.TopicStatus
import app.yap.server.feature.topics.persistence.StubTopicRepository
import app.yap.server.feature.topics.persistence.TopicPublish
import app.yap.server.feature.topics.persistence.TopicWrite
import app.yap.server.feature.topics.usecase.CreateTopicUseCase
import app.yap.server.feature.topics.usecase.GetTopicCalendarUseCase
import app.yap.server.feature.topics.usecase.GetTopicUseCase
import app.yap.server.feature.topics.usecase.GetTopicsUseCase
import app.yap.server.feature.topics.usecase.PublishTopicUseCase
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

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
    fun `GIVEN a draft with every kind of gap WHEN reading it THEN the server names each gap by its type`() =
        testApplication {
            val env = Environment()
            env.topicRepository.findCall.returns(
                StubTopic.stubTopic(
                    content = StubTopic.stubContent(
                        releaseDate = StubTopic.RELEASE_DATE.minusDays(7),
                        setting = "",
                        steps = listOf(StubTopic.stubStep(outcome = "")),
                    ),
                ),
            )
            env.topicRepository.hasPublishedOnCall.returns(true)
            application { env.install(this) }

            val response = client.get("/admin/topics/${StubTopicDto.TOPIC_ID}")

            assertEquals(
                expected = Json.parseToJsonElement(
                    """
                    [
                      {"type": "missing", "field": "SETTING"},
                      {"type": "pastDate"},
                      {"type": "incompleteSteps", "numbers": [1]},
                      {"type": "dateTaken"}
                    ]
                    """,
                ),
                actual = Json.parseToJsonElement(response.bodyAsText()).jsonObject["gaps"],
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
        env.topicRepository.findCall.returns(null)
        application { env.install(this) }

        val response = putTopic(contentJson())

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    @Test
    fun `GIVEN a date with a published topic WHEN updating a topic THEN the server answers 409`() = testApplication {
        val env = Environment()
        env.topicRepository.updateCall.returns(TopicWrite.DateTaken)
        application { env.install(this) }

        val response = putTopic(contentJson())

        assertEquals(expected = HttpStatusCode.Conflict, actual = response.status)
    }

    @Test
    fun `GIVEN a topic WHEN reading the calendar THEN the server answers with the rows and To do`() = testApplication {
        application { Environment().install(this) }

        val response = client.get("/admin/topics/calendar")

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(
            expected = listOf(
                HttpStatusCode.OK,
                14,
                Json.parseToJsonElement(
                    """
                    {
                      "date": "${StubTopicDto.RELEASE_DATE}",
                      "topic": {
                        "completeSteps": 1,
                        "ledBy": {"type": "character", "name": "${StubCharacter.NAME}"},
                        "status": "DRAFT",
                        "steps": 1,
                        "title": "${StubTopic.TITLE}",
                        "topicId": "${StubTopicDto.TOPIC_ID}"
                      }
                    }
                    """,
                ),
                Json.parseToJsonElement(
                    """
                    [
                      {"type": "emptyDay", "date": "2026-10-23"},
                      {"type": "draft", "date": "${StubTopicDto.RELEASE_DATE}", "gaps": [], "title": "${StubTopic.TITLE}", "topicId": "${StubTopicDto.TOPIC_ID}"}
                    ]
                    """,
                ),
            ),
            actual = listOf(
                response.status,
                body.getValue("rows").jsonArray.size,
                body.getValue("rows").jsonArray[1],
                JsonArray(body.getValue("todos").jsonArray.take(2)),
            ),
        )
    }

    @Test
    fun `GIVEN a title of spaces WHEN creating a topic THEN the server answers 400 with the missing title`() =
        testApplication {
            application { Environment().install(this) }

            val response = postTopic(Json.encodeToString(StubTopicDto.stubContentDto(title = "  ")))

            assertEquals(
                expected = HttpStatusCode.BadRequest to Json.parseToJsonElement(
                    """{"gaps": [{"type": "missing", "field": "TITLE"}]}""",
                ),
                actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
            )
        }

    @Test
    fun `GIVEN a published topic with a past date WHEN updating it THEN the server answers 400 with the gaps`() =
        testApplication {
            application { Environment(StubTopic.stubTopic(status = TopicStatus.PUBLISHED)).install(this) }

            val response = putTopic(
                Json.encodeToString(StubTopicDto.stubContentDto(releaseDate = PAST_DATE, setting = "")),
            )

            assertEquals(expected = HttpStatusCode.BadRequest to REJECTION, actual = response.status to response.json())
        }

    @Test
    fun `GIVEN a published topic WHEN updating it to a taken date THEN the server answers 409`() = testApplication {
        val env = Environment(StubTopic.stubTopic(status = TopicStatus.PUBLISHED))
        env.topicRepository.updateCall.returns(TopicWrite.DateTaken)
        application { env.install(this) }

        val response = putTopic(contentJson())

        assertEquals(expected = HttpStatusCode.Conflict, actual = response.status)
    }

    @Test
    fun `GIVEN a filled draft WHEN publishing it THEN the server answers with the published topic`() = testApplication {
        val env = Environment()
        env.topicRepository.publishCall.returns(
            TopicPublish.Published(StubTopic.stubTopic(status = TopicStatus.PUBLISHED)),
        )
        application { env.install(this) }

        val response = publishTopic(contentJson())

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(topicJson(status = "PUBLISHED")),
            actual = response.status to response.json(),
        )
    }

    @Test
    fun `GIVEN a published topic WHEN publishing it THEN the server answers with it`() = testApplication {
        application { Environment(StubTopic.stubTopic(status = TopicStatus.PUBLISHED)).install(this) }

        val response = publishTopic(contentJson())

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(topicJson(status = "PUBLISHED")),
            actual = response.status to response.json(),
        )
    }

    @Test
    fun `GIVEN a draft with gaps and a past date WHEN publishing it THEN the server answers 400 with the gaps`() =
        testApplication {
            application { Environment().install(this) }

            val response = publishTopic(
                Json.encodeToString(StubTopicDto.stubContentDto(releaseDate = PAST_DATE, setting = "")),
            )

            assertEquals(expected = HttpStatusCode.BadRequest to REJECTION, actual = response.status to response.json())
        }

    @Test
    fun `GIVEN an unknown topic WHEN publishing it THEN the server answers 404`() = testApplication {
        val env = Environment()
        env.topicRepository.findCall.returns(null)
        application { env.install(this) }

        val response = publishTopic(contentJson())

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    @Test
    fun `GIVEN a date with a published topic WHEN publishing a draft THEN the server answers 409`() = testApplication {
        val env = Environment()
        env.topicRepository.publishCall.returns(TopicPublish.DateTaken)
        application { env.install(this) }

        val response = publishTopic(contentJson())

        assertEquals(expected = HttpStatusCode.Conflict, actual = response.status)
    }

    private class Environment(topic: Topic = StubTopic.stubTopic()) {

        val characterRepository = StubCharacterRepository()
        val topicRepository = StubTopicRepository(topic)

        fun install(application: Application) {
            val topicDraftRule = TopicDraftRule(
                getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
            )
            val topicPublishRule = TopicPublishRule(topicDraftRule = topicDraftRule)
            val topicCalendarRule = TopicCalendarRule(topicPublishRule = topicPublishRule)
            val topicDetailsRule = TopicDetailsRule(topicPublishRule = topicPublishRule)
            application.install(ContentNegotiation) {
                json()
            }
            application.routing {
                topicRoutes(
                    createTopicUseCase = CreateTopicUseCase(
                        clock = StubTopic.CLOCK,
                        topicDetailsRule = topicDetailsRule,
                        topicDraftRule = topicDraftRule,
                        topicRepository = topicRepository,
                    ),
                    getTopicCalendarUseCase = GetTopicCalendarUseCase(
                        clock = StubTopic.CLOCK,
                        getCharactersUseCase = GetCharactersUseCase(characterRepository = characterRepository),
                        topicCalendarRule = topicCalendarRule,
                        topicRepository = topicRepository,
                    ),
                    getTopicUseCase = GetTopicUseCase(
                        clock = StubTopic.CLOCK,
                        topicDetailsRule = topicDetailsRule,
                        topicRepository = topicRepository,
                    ),
                    getTopicsUseCase = GetTopicsUseCase(
                        clock = StubTopic.CLOCK,
                        topicDetailsRule = topicDetailsRule,
                        topicRepository = topicRepository,
                    ),
                    publishTopicUseCase = PublishTopicUseCase(
                        clock = StubTopic.CLOCK,
                        topicDetailsRule = topicDetailsRule,
                        topicPublishRule = topicPublishRule,
                        topicRepository = topicRepository,
                    ),
                    updateTopicUseCase = UpdateTopicUseCase(
                        clock = StubTopic.CLOCK,
                        topicDetailsRule = topicDetailsRule,
                        topicDraftRule = topicDraftRule,
                        topicPublishRule = topicPublishRule,
                        topicRepository = topicRepository,
                    ),
                )
            }
        }
    }

    private companion object {

        const val CAST_JSON = """{"type": "character", "characterId": "${StubTopicDto.CHARACTER_ID}"}"""
        const val GUEST_DESCRIPTION = "A tourist who asks for directions."
        const val PAST_DATE = "2026-10-17"
        const val GUEST_JSON = """{"type": "guest", "description": "$GUEST_DESCRIPTION"}"""

        val REJECTION = Json.parseToJsonElement(
            """{"gaps": [{"type": "missing", "field": "SETTING"}, {"type": "pastDate"}]}""",
        )

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

        fun topicJson(
            interlocutor: String = CAST_JSON,
            status: String = "DRAFT",
        ): String = """
            {
              "content": ${contentJson(interlocutor)},
              "gaps": [],
              "status": "$status",
              "topicId": "${StubTopicDto.TOPIC_ID}"
            }
        """
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

private suspend fun ApplicationTestBuilder.publishTopic(body: String): HttpResponse =
    client.post("/admin/topics/${StubTopicDto.TOPIC_ID}/publish") {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

private suspend fun HttpResponse.json(): JsonElement = Json.parseToJsonElement(bodyAsText())
