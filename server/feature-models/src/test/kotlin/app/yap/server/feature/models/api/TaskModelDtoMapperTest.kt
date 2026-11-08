package app.yap.server.feature.models.api

import app.yap.server.feature.models.model.StubTaskModel
import io.ktor.server.plugins.BadRequestException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class TaskModelDtoMapperTest {

    @Test
    fun `GIVEN a table of task DTOs WHEN mapping it to the models THEN every task has its choice`() {
        val dtos = StubTaskModelDto.stubTableDtos(review = StubTaskModelDto.stubReviewDto())

        val models = dtos.toModels()

        assertEquals(expected = StubTaskModel.stubTable(review = StubTaskModel.stubChoice()), actual = models)
    }

    @Test
    fun `GIVEN a task code that is not a task WHEN mapping the table THEN it fails with a bad request`() {
        val dtos = listOf(StubTaskModelDto.stubTaskModelDto(task = "SUMMARY"))

        assertFailsWith<BadRequestException> { dtos.toModels() }
    }

    @Test
    fun `GIVEN a provider code that is not a provider WHEN mapping the table THEN there are no models`() {
        val dtos = listOf(StubTaskModelDto.stubTaskModelDto(provider = "OPENAI"))

        assertNull(dtos.toModels())
    }
}
