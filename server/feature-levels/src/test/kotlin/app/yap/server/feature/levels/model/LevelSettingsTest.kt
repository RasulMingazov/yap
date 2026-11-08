package app.yap.server.feature.levels.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class LevelSettingsTest {

    @Test
    fun `GIVEN a vocabulary and a block with spaces around WHEN normalizing the settings THEN both are trimmed`() {
        val settings = StubLevel.stubSettings(block = " Speak simply. ", vocabulary = " B1 ")

        val normalized = settings.normalized()

        assertEquals(
            expected = StubLevel.stubSettings(block = "Speak simply.", vocabulary = "B1"),
            actual = normalized,
        )
    }
}
