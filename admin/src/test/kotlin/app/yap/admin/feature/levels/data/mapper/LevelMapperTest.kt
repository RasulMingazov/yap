package app.yap.admin.feature.levels.data.mapper

import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.StubLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class LevelMapperTest {

    @Test
    fun `GIVEN a level with settings and a range WHEN mapping it THEN the level holds every field`() {
        val dto = StubLevelDto.stubLevelDto()

        val level = dto.toDomain()

        assertEquals(expected = StubLevel.stubLevel(), actual = level)
    }

    @Test
    fun `GIVEN a level without settings and a range WHEN mapping it THEN the level has neither`() {
        val dto = StubLevelDto.stubLevelDto(code = "NO_RATING", range = null, settings = null)

        val level = dto.toDomain()

        assertEquals(
            expected = StubLevel.stubLevel(code = LevelCode.NO_RATING, range = null, settings = null),
            actual = level,
        )
    }

    @Test
    fun `GIVEN the last level WHEN mapping it THEN its range has no end`() {
        val dto = StubLevelDto.stubLevelDto(code = "C2", range = StubLevelDto.stubRangeDto(to = null))

        val level = dto.toDomain()

        assertEquals(expected = StubLevel.stubRange(to = null), actual = level.range)
    }

    @Test
    fun `GIVEN an unknown level code WHEN mapping it THEN mapping fails`() {
        val dto = StubLevelDto.stubLevelDto(code = "D1")

        assertFailsWith<IllegalArgumentException> { dto.toDomain() }
    }

    @Test
    fun `GIVEN an unknown idioms setting WHEN mapping the level THEN mapping fails`() {
        val dto = StubLevelDto.stubLevelDto(settings = StubLevelDto.stubSettingsDto(idioms = "OFTEN"))

        assertFailsWith<IllegalArgumentException> { dto.toDomain() }
    }

    @Test
    fun `GIVEN an entry WHEN mapping it THEN the DTO holds the names and the values`() {
        val entry = StubLevel.stubEntry()

        val dto = entry.toDto()

        assertEquals(expected = StubLevelDto.stubInputDto(), actual = dto)
    }
}
