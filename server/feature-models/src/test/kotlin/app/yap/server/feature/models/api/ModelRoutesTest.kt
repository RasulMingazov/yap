package app.yap.server.feature.models.api

import app.yap.contract.admin.TaskModelDto
import app.yap.server.core.llm.LlmConfig
import app.yap.server.core.llm.ProviderConnections
import app.yap.server.core.llm.StubLlmConfig
import app.yap.server.feature.models.model.StubTaskModel
import app.yap.server.feature.models.model.TaskModel
import app.yap.server.feature.models.persistence.StubTaskModelRepository
import app.yap.server.feature.models.usecase.GetModelsUseCase
import app.yap.server.feature.models.usecase.SaveModelsUseCase
import io.ktor.client.request.get
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

internal class ModelRoutesTest {

    @Test
    fun `GIVEN the server has a DeepSeek key WHEN reading the models THEN DeepSeek is connected and no key shows`() = testApplication {
        application { Environment().install(this) }

        val response = client.get("/admin/models")

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(modelsJson(connected = true, reviewModel = "deepseek-flash")),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN the server has no DeepSeek key WHEN reading the models THEN DeepSeek is not connected`() = testApplication {
        application { Environment(llmConfig = StubLlmConfig.stubLlmConfig(deepSeek = null)).install(this) }

        val response = client.get("/admin/models")

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(modelsJson(connected = false, reviewModel = "deepseek-flash")),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a full table WHEN saving the models THEN the server stores it and answers with the models`() = testApplication {
        val table = StubTaskModel.stubTable(review = StubTaskModel.stubChoice())
        val env = Environment(models = table)
        application { env.install(this) }

        val response = putModels(StubTaskModelDto.stubTableDtos(review = StubTaskModelDto.stubReviewDto()))

        env.taskModelRepository.saveCall.calledWith(table)
        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(modelsJson(connected = true, reviewModel = "deepseek-v4-pro")),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a task code that is not a task WHEN saving the models THEN the server answers 400`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = putModels(listOf(StubTaskModelDto.stubTaskModelDto(task = "SUMMARY")))

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
        env.taskModelRepository.saveCall.notCalled()
    }

    @Test
    fun `GIVEN a table without Hints WHEN saving the models THEN the server answers 400`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = putModels(
            StubTaskModelDto
                .stubTableDtos()
                .filter { dto -> dto.task != "TOPIC_HINTS" },
        )

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
        env.taskModelRepository.saveCall.notCalled()
    }

    @Test
    fun `GIVEN a provider that is not in the catalog WHEN saving the models THEN the server answers 409`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = putModels(
            StubTaskModelDto.stubTableDtos(
                review = StubTaskModelDto.stubTaskModelDto(provider = "OPENAI", task = "TOPIC_REVIEW"),
            ),
        )

        assertEquals(expected = HttpStatusCode.Conflict, actual = response.status)
        env.taskModelRepository.saveCall.notCalled()
    }

    @Test
    fun `GIVEN a model that is not in the catalog WHEN saving the models THEN the server answers 409`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = putModels(StubTaskModelDto.stubTableDtos(review = StubTaskModelDto.stubReviewDto(model = "deepseek-chat")))

        assertEquals(expected = HttpStatusCode.Conflict, actual = response.status)
        env.taskModelRepository.saveCall.notCalled()
    }

    private class Environment(
        private val llmConfig: LlmConfig = StubLlmConfig.stubLlmConfig(),
        models: List<TaskModel> = StubTaskModel.stubTable(),
    ) {

        val taskModelRepository = StubTaskModelRepository(models = models)

        fun install(application: Application) {
            val getModelsUseCase = GetModelsUseCase(
                providerConnections = ProviderConnections(config = llmConfig),
                taskModelRepository = taskModelRepository,
            )
            application.install(ContentNegotiation) {
                json()
            }
            application.routing {
                modelRoutes(
                    getModelsUseCase = getModelsUseCase,
                    saveModelsUseCase = SaveModelsUseCase(
                        getModelsUseCase = getModelsUseCase,
                        taskModelRepository = taskModelRepository,
                    ),
                )
            }
        }
    }

    private companion object {

        fun modelsJson(connected: Boolean, reviewModel: String): String = """
            {
              "providers": [
                {
                  "code": "DEEPSEEK",
                  "connected": $connected,
                  "keySetting": "DEEPSEEK_API_KEY",
                  "models": ["deepseek-flash", "deepseek-v4-pro"],
                  "name": "DeepSeek"
                }
              ],
              "tasks": [
                {"model": "deepseek-flash", "provider": "DEEPSEEK", "task": "TOPIC_REPLY"},
                {"model": "deepseek-flash", "provider": "DEEPSEEK", "task": "TOPIC_CHECK"},
                {"model": "deepseek-flash", "provider": "DEEPSEEK", "task": "TOPIC_HINTS"},
                {"model": "$reviewModel", "provider": "DEEPSEEK", "task": "TOPIC_REVIEW"},
                {"model": "deepseek-flash", "provider": "DEEPSEEK", "task": "CALIBRATION"}
              ]
            }
        """
    }
}

private suspend fun ApplicationTestBuilder.putModels(models: List<TaskModelDto>): HttpResponse =
    client.put("/admin/models") {
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(models))
    }
