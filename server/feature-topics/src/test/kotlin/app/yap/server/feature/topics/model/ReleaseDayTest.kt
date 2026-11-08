package app.yap.server.feature.topics.model

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReleaseDayTest {

    @Test
    fun `GIVEN 22 30 UTC on 23 October WHEN reading the Moscow day THEN it is 24 October`() {
        val clock = Clock.fixed(Instant.parse("2026-10-23T22:30:00Z"), ZoneOffset.UTC)

        val day = moscowToday(clock)

        assertEquals(expected = LocalDate.of(2026, 10, 24), actual = day)
    }
}
