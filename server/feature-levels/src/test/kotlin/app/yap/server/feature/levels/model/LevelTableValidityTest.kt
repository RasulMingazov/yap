package app.yap.server.feature.levels.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LevelTableValidityTest {

    @Test
    fun `GIVEN every level once with filled values and growing ratings WHEN checking the table THEN it is valid`() {
        val table = StubLevel.stubTable()

        assertTrue(LevelTableValidity.isValid(table))
    }

    @Test
    fun `GIVEN a table without C2 WHEN checking the table THEN it is not valid`() {
        val table = StubLevel.stubTable().filter { entry -> entry.code != LevelCode.C2 }

        assertFalse(LevelTableValidity.isValid(table))
    }

    @Test
    fun `GIVEN A1 twice WHEN checking the table THEN it is not valid`() {
        val table = StubLevel.stubTable() + StubLevel.stubTable().first()

        assertFalse(LevelTableValidity.isValid(table))
    }

    @Test
    fun `GIVEN an empty block WHEN checking the table THEN it is not valid`() {
        val table = StubLevel.stubTable(b1 = StubLevel.stubSettings(block = ""))

        assertFalse(LevelTableValidity.isValid(table))
    }

    @Test
    fun `GIVEN a vocabulary of only spaces WHEN checking the table THEN it is not valid`() {
        val table = StubLevel.stubTable(b1 = StubLevel.stubSettings(vocabulary = "   "))

        assertFalse(LevelTableValidity.isValid(table))
    }

    @Test
    fun `GIVEN zero words WHEN checking the table THEN it is not valid`() {
        val table = StubLevel.stubTable(b1 = StubLevel.stubSettings(maxWords = 0))

        assertFalse(LevelTableValidity.isValid(table))
    }

    @Test
    fun `GIVEN No rating with a rating from WHEN checking the table THEN it is not valid`() {
        val table = StubLevel.stubTable(noRating = StubLevel.stubSettings(ratingFrom = 1000))

        assertFalse(LevelTableValidity.isValid(table))
    }

    @Test
    fun `GIVEN B1 without a rating from WHEN checking the table THEN it is not valid`() {
        val table = StubLevel.stubTable(b1 = StubLevel.stubSettings(ratingFrom = null))

        assertFalse(LevelTableValidity.isValid(table))
    }

    @Test
    fun `GIVEN B2 with a rating from below B1 WHEN checking the table THEN it is not valid`() {
        val table = StubLevel.stubTable(b2 = StubLevel.stubSettings(ratingFrom = StubLevel.RATING_FROM - 1))

        assertFalse(LevelTableValidity.isValid(table))
    }
}
