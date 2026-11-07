package app.yap.server.feature.topics.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TopicContentTest {

    @Test
    fun `GIVEN only a title and a date WHEN checking the draft THEN the draft is ready`() {
        val content = StubTopic.stubContent(
            description = "",
            durationMinutes = null,
            interlocutor = null,
            openingLine = StubTopic.stubOpeningLine(english = "", translation = ""),
            role = "",
            setting = "",
            steps = emptyList(),
        )

        val isDraftReady = content.isDraftReady

        assertTrue(isDraftReady)
    }

    @Test
    fun `GIVEN a guest with an empty description WHEN checking the draft THEN the draft is ready`() {
        val content = StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = ""))

        val isDraftReady = content.isDraftReady

        assertTrue(isDraftReady)
    }

    @Test
    fun `GIVEN an empty title WHEN checking the draft THEN the draft is not ready`() {
        val content = StubTopic.stubContent(title = "")

        val isDraftReady = content.isDraftReady

        assertFalse(isDraftReady)
    }

    @Test
    fun `GIVEN no release date WHEN checking the draft THEN the draft is not ready`() {
        val content = StubTopic.stubContent(releaseDate = null)

        val isDraftReady = content.isDraftReady

        assertFalse(isDraftReady)
    }

    @Test
    fun `GIVEN a duration of 0 minutes WHEN checking the draft THEN the draft is not ready`() {
        val content = StubTopic.stubContent(durationMinutes = 0)

        val isDraftReady = content.isDraftReady

        assertFalse(isDraftReady)
    }

    @Test
    fun `GIVEN a duration of 1 minute WHEN checking the draft THEN the draft is ready`() {
        val content = StubTopic.stubContent(durationMinutes = 1)

        val isDraftReady = content.isDraftReady

        assertTrue(isDraftReady)
    }
}
