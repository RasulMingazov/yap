package app.yap.server.feature.models.api

import kotlin.test.Test
import kotlin.test.assertNull

internal class TaskModelDtoMapperTest {

    @Test
    fun `GIVEN a provider code that is not a provider WHEN mapping the table THEN there are no models`() {
        val dtos = listOf(StubTaskModelDto.stubTaskModelDto(provider = "OPENAI"))

        assertNull(dtos.toModels())
    }
}
