package app.yap.server.feature.prompts.api

import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.model.StubPrompt
import app.yap.server.feature.prompts.persistence.StubPromptRepository
import app.yap.server.feature.prompts.usecase.GetPromptUseCase
import app.yap.server.feature.prompts.usecase.GetPromptsUseCase
import app.yap.server.feature.prompts.usecase.RestorePromptUseCase
import app.yap.server.feature.prompts.usecase.SavePromptUseCase
import io.ktor.client.request.get
import io.ktor.client.request.post
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

internal class PromptRoutesTest {

    @Test
    fun `GIVEN the six prompts WHEN listing them THEN the server answers with their keys and groups`() = testApplication {
        application { Environment().install(this) }

        val response = client.get("/admin/prompts")

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(PROMPTS_JSON),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a saved prompt WHEN reading it THEN the server answers with its text, variables and versions`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.get("/admin/prompts/TOPIC_REPLY")

        env.promptRepository.getCall.calledWith(PromptKey.TOPIC_REPLY)
        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(PROMPT_JSON),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a key that is not a prompt WHEN reading it THEN the server answers 404`() = testApplication {
        application { Environment().install(this) }

        val response = client.get("/admin/prompts/TOPIC_PLOT")

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    @Test
    fun `GIVEN a text WHEN saving a version THEN the server answers 201 with the prompt`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = postVersion("""{"text": "${StubPrompt.TEXT}"}""")

        env.promptRepository.saveCall.calledWith(PromptKey.TOPIC_REPLY, StubPrompt.TEXT)
        assertEquals(
            expected = HttpStatusCode.Created to Json.parseToJsonElement(PROMPT_JSON),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a text of spaces WHEN saving a version THEN the server answers 400 and stores nothing`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = postVersion("""{"text": "   "}""")

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
        env.promptRepository.saveCall.notCalled()
    }

    @Test
    fun `GIVEN a saved version WHEN restoring it THEN the server answers with the prompt`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = client.post("/admin/prompts/TOPIC_REPLY/versions/1/restore")

        env.promptRepository.restoreCall.calledWith(PromptKey.TOPIC_REPLY, 1)
        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(PROMPT_JSON),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN an unknown version WHEN restoring it THEN the server answers 404`() = testApplication {
        val env = Environment()
        env.promptRepository.restoreCall.returns(null)
        application { env.install(this) }

        val response = client.post("/admin/prompts/TOPIC_REPLY/versions/7/restore")

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    @Test
    fun `GIVEN a version that is not a number WHEN restoring it THEN the server answers 404`() = testApplication {
        application { Environment().install(this) }

        val response = client.post("/admin/prompts/TOPIC_REPLY/versions/last/restore")

        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    private class Environment {

        val promptRepository = StubPromptRepository()

        fun install(application: Application) {
            application.install(ContentNegotiation) {
                json()
            }
            application.routing {
                promptRoutes(
                    getPromptUseCase = GetPromptUseCase(promptRepository = promptRepository),
                    getPromptsUseCase = GetPromptsUseCase(),
                    restorePromptUseCase = RestorePromptUseCase(promptRepository = promptRepository),
                    savePromptUseCase = SavePromptUseCase(promptRepository = promptRepository),
                )
            }
        }
    }

    private companion object {

        const val PROMPTS_JSON = """
            [
              {"group": "TOPIC", "promptKey": "TOPIC_REPLY"},
              {"group": "TOPIC", "promptKey": "TOPIC_CHECK"},
              {"group": "TOPIC", "promptKey": "TOPIC_HINTS"},
              {"group": "TOPIC", "promptKey": "TOPIC_REVIEW"},
              {"group": "CALIBRATION", "promptKey": "CALIBRATION_CHECK"},
              {"group": "CALIBRATION", "promptKey": "CALIBRATION_REPLY"}
            ]
        """

        const val PROMPT_JSON = """
            {
              "group": "TOPIC",
              "promptKey": "TOPIC_REPLY",
              "text": "${StubPrompt.TEXT}",
              "variables": [
                "{interlocutor}", "{topic.setting}", "{topic.role}", "{level.rules}",
                "{step.n}", "{step.task}", "{check.verdict}"
              ],
              "versions": [{"isCurrent": true, "number": 1, "savedAt": "2026-10-24T09:30:00Z"}]
            }
        """
    }
}

private suspend fun ApplicationTestBuilder.postVersion(body: String): HttpResponse =
    client.post("/admin/prompts/TOPIC_REPLY/versions") {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
