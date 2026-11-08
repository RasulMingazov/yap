package app.yap.server.feature.levels.api

import app.yap.contract.admin.LevelInputDto
import app.yap.server.feature.levels.model.StubLevel
import app.yap.server.feature.levels.persistence.StubLevelRepository
import app.yap.server.feature.levels.usecase.GetLevelsUseCase
import app.yap.server.feature.levels.usecase.SaveLevelsUseCase
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

internal class LevelRoutesTest {

    @Test
    fun `GIVEN a saved table WHEN listing the levels THEN the server answers with their JSON`() = testApplication {
        application { Environment().install(this) }

        val response = client.get("/admin/levels")

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(levelsJson()),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN no saved levels WHEN listing them THEN the server answers with seven empty levels`() = testApplication {
        val env = Environment()
        env.levelRepository.listCall.returns(emptyList())
        application { env.install(this) }

        val response = client.get("/admin/levels")

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(UNSAVED_LEVELS_JSON),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a filled table WHEN saving the levels THEN the server answers with their JSON`() = testApplication {
        application { Environment().install(this) }

        val response = putLevels(StubLevelDto.stubInputDtos())

        assertEquals(
            expected = HttpStatusCode.OK to Json.parseToJsonElement(levelsJson()),
            actual = response.status to Json.parseToJsonElement(response.bodyAsText()),
        )
    }

    @Test
    fun `GIVEN a level with a model WHEN saving the levels THEN the server stores the model`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = putLevels(StubLevelDto.stubInputDtos(b1 = StubLevelDto.stubSettingsDto(model = StubLevel.MODEL)))

        assertEquals(expected = HttpStatusCode.OK, actual = response.status)
        env.levelRepository.saveCall.calledWith(
            StubLevel.stubTable(b1 = StubLevel.stubSettings(model = StubLevel.MODEL)),
        )
    }

    @Test
    fun `GIVEN a model outside the catalog WHEN saving the levels THEN the server answers 409`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = putLevels(StubLevelDto.stubInputDtos(b1 = StubLevelDto.stubSettingsDto(model = "deepseek-chat")))

        assertEquals(expected = HttpStatusCode.Conflict, actual = response.status)
        env.levelRepository.saveCall.notCalled()
    }

    @Test
    fun `GIVEN a code that is not a level WHEN saving the levels THEN the server answers 400`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = putLevels(listOf(StubLevelDto.stubInputDto(code = "D1")))

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
        env.levelRepository.saveCall.notCalled()
    }

    @Test
    fun `GIVEN a table without C2 WHEN saving the levels THEN the server answers 400`() = testApplication {
        val env = Environment()
        application { env.install(this) }

        val response = putLevels(StubLevelDto.stubInputDtos().filter { input -> input.code != "C2" })

        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
        env.levelRepository.saveCall.notCalled()
    }

    private class Environment {

        val levelRepository = StubLevelRepository()

        fun install(application: Application) {
            application.install(ContentNegotiation) {
                json()
            }
            application.routing {
                levelRoutes(
                    getLevelsUseCase = GetLevelsUseCase(levelRepository = levelRepository),
                    saveLevelsUseCase = SaveLevelsUseCase(levelRepository = levelRepository),
                )
            }
        }
    }

    private companion object {

        const val UNSAVED_LEVELS_JSON = """
            [
              {"code": "A1", "range": null, "settings": null},
              {"code": "A2", "range": null, "settings": null},
              {"code": "B1", "range": null, "settings": null},
              {"code": "B2", "range": null, "settings": null},
              {"code": "C1", "range": null, "settings": null},
              {"code": "C2", "range": null, "settings": null},
              {"code": "NO_RATING", "range": null, "settings": null}
            ]
        """

        fun levelsJson(): String = listOf(
            levelJson(code = "A1", range = """{"from": 600, "to": 899}""", ratingFrom = 600),
            levelJson(code = "A2", range = """{"from": 900, "to": 1049}""", ratingFrom = 900),
            levelJson(code = "B1", range = """{"from": 1050, "to": 1199}""", ratingFrom = 1050),
            levelJson(code = "B2", range = """{"from": 1200, "to": 1349}""", ratingFrom = 1200),
            levelJson(code = "C1", range = """{"from": 1350, "to": 1499}""", ratingFrom = 1350),
            levelJson(code = "C2", range = """{"from": 1500, "to": null}""", ratingFrom = 1500),
            levelJson(code = "NO_RATING", range = "null", ratingFrom = null),
        ).joinToString(prefix = "[", postfix = "]")

        fun levelJson(code: String, range: String, ratingFrom: Int?): String = """
            {
              "code": "$code",
              "range": $range,
              "settings": {
                "block": "${StubLevel.BLOCK}",
                "idioms": "RARELY",
                "maxWords": ${StubLevel.MAX_WORDS},
                "model": null,
                "ratingFrom": $ratingFrom,
                "vocabulary": "${StubLevel.VOCABULARY}"
              }
            }
        """
    }
}

private suspend fun ApplicationTestBuilder.putLevels(inputs: List<LevelInputDto>): HttpResponse =
    client.put("/admin/levels") {
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(inputs))
    }
