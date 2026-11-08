package app.yap.server.feature.topics.model

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReleaseStateTest {

    private val today = LocalDate.of(2026, 10, 23)

    @Test
    fun `GIVEN a published topic of an earlier date WHEN deriving the state THEN it is out`() {
        val state = ReleaseState.of(TopicStatus.PUBLISHED, LocalDate.of(2026, 10, 22), today)

        assertEquals(expected = ReleaseState.OUT, actual = state)
    }

    @Test
    fun `GIVEN a published topic of today WHEN deriving the state THEN it is today`() {
        val state = ReleaseState.of(TopicStatus.PUBLISHED, today, today)

        assertEquals(expected = ReleaseState.TODAY, actual = state)
    }

    @Test
    fun `GIVEN a published topic of a later date WHEN deriving the state THEN it is published`() {
        val state = ReleaseState.of(TopicStatus.PUBLISHED, LocalDate.of(2026, 10, 24), today)

        assertEquals(expected = ReleaseState.PUBLISHED, actual = state)
    }

    @Test
    fun `GIVEN a draft of today WHEN deriving the state THEN it is a draft`() {
        val state = ReleaseState.of(TopicStatus.DRAFT, today, today)

        assertEquals(expected = ReleaseState.DRAFT, actual = state)
    }

    @Test
    fun `GIVEN 22 30 UTC on 23 October WHEN reading the Moscow day THEN it is 24 October`() {
        val clock = Clock.fixed(Instant.parse("2026-10-23T22:30:00Z"), ReleaseState.MOSCOW)

        val day = LocalDate.now(clock)

        assertEquals(expected = LocalDate.of(2026, 10, 24), actual = day)
    }
}
