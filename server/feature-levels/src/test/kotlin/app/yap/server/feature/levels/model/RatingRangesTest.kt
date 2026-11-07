package app.yap.server.feature.levels.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class RatingRangesTest {

    @Test
    fun `GIVEN saved levels in any order WHEN deriving the levels THEN they come in table order with ranges`() {
        val table = StubLevel.stubTable()
        val ranges = listOf(
            RatingRange(from = 600, to = 899),
            RatingRange(from = 900, to = 1049),
            RatingRange(from = 1050, to = 1199),
            RatingRange(from = 1200, to = 1349),
            RatingRange(from = 1350, to = 1499),
            RatingRange(from = 1500, to = null),
            null,
        )

        val levels = RatingRanges.levelsOf(table.reversed())

        assertEquals(
            expected = table.zip(ranges) { entry, range ->
                Level(code = entry.code, range = range, settings = entry.settings)
            },
            actual = levels,
        )
    }

    @Test
    fun `GIVEN no saved levels WHEN deriving the levels THEN all seven come in order with no values`() {
        val levels = RatingRanges.levelsOf(emptyList())

        assertEquals(
            expected = LevelCode.entries.map { code -> Level(code = code, range = null, settings = null) },
            actual = levels,
        )
    }
}
