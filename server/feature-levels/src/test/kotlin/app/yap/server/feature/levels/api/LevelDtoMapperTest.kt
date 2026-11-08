package app.yap.server.feature.levels.api

import app.yap.contract.admin.LevelDto
import app.yap.contract.admin.RatingRangeDto
import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.levels.model.LevelEntry
import app.yap.server.feature.levels.model.StubLevel
import io.ktor.server.plugins.BadRequestException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class LevelDtoMapperTest {

    @Test
    fun `GIVEN a vocabulary and a block with spaces around WHEN mapping the input THEN both stay as they are`() {
        val dto = StubLevelDto.stubInputDto(
            settings = StubLevelDto.stubSettingsDto(block = " Speak simply. ", vocabulary = " B1 "),
        )

        val entry = dto.toModel()

        assertEquals(
            expected = LevelEntry(
                code = LevelCode.B1,
                settings = StubLevel.stubSettings(block = " Speak simply. ", vocabulary = " B1 "),
            ),
            actual = entry,
        )
    }

    @Test
    fun `GIVEN an input with a model WHEN mapping it THEN the entry keeps the model`() {
        val dto = StubLevelDto.stubInputDto(settings = StubLevelDto.stubSettingsDto(model = StubLevel.MODEL))

        val entry = dto.toModel()

        assertEquals(expected = StubLevel.MODEL, actual = entry.settings.model)
    }

    @Test
    fun `GIVEN a code that is not a level WHEN mapping the input THEN it fails with a bad request`() {
        val dto = StubLevelDto.stubInputDto(code = "D1")

        assertFailsWith<BadRequestException> { dto.toModel() }
    }

    @Test
    fun `GIVEN an idioms setting that does not exist WHEN mapping the input THEN it fails with a bad request`() {
        val dto = StubLevelDto.stubInputDto(settings = StubLevelDto.stubSettingsDto(idioms = "OFTEN"))

        assertFailsWith<BadRequestException> { dto.toModel() }
    }

    @Test
    fun `GIVEN a level with values WHEN mapping it to the DTO THEN the DTO holds every field`() {
        val level = StubLevel.stubLevel()

        val dto = level.toDto()

        assertEquals(
            expected = LevelDto(
                code = "B1",
                range = RatingRangeDto(from = 1050, to = 1199),
                settings = StubLevelDto.stubSettingsDto(),
            ),
            actual = dto,
        )
    }

    @Test
    fun `GIVEN a level with a model WHEN mapping it to the DTO THEN the DTO holds the model`() {
        val level = StubLevel.stubLevel(settings = StubLevel.stubSettings(model = StubLevel.MODEL))

        val dto = level.toDto()

        assertEquals(expected = StubLevel.MODEL, actual = dto.settings?.model)
    }
}
