package app.yap.server.feature.topics.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TopicContentTest {

    @Test
    fun `GIVEN texts with spaces around WHEN checking the draft THEN every text is trimmed`() {
        val content = StubTopic.stubContent(
            description = " Order a coffee. ",
            interlocutor = Interlocutor.Guest(description = " A tourist. "),
            openingLine = StubTopic.stubOpeningLine(english = " Hi! ", translation = " Привет! "),
            role = " A barista. ",
            setting = " A cafe. ",
            steps = listOf(StubTopic.stubStep(outcome = " Done. ", passWhen = " Names it. ", task = " Order. ")),
            title = " At the cafe ",
        )

        val checked = content.trimmedIfDraft()

        assertEquals(
            expected = StubTopic.stubContent(
                description = "Order a coffee.",
                interlocutor = Interlocutor.Guest(description = "A tourist."),
                openingLine = StubTopic.stubOpeningLine(english = "Hi!", translation = "Привет!"),
                role = "A barista.",
                setting = "A cafe.",
                steps = listOf(StubTopic.stubStep(outcome = "Done.", passWhen = "Names it.", task = "Order.")),
                title = "At the cafe",
            ),
            actual = checked,
        )
    }

    @Test
    fun `GIVEN only a title and a date WHEN checking the draft THEN the draft is valid`() {
        val content = StubTopic.stubContent(
            description = "",
            durationMinutes = null,
            interlocutor = null,
            openingLine = StubTopic.stubOpeningLine(english = "", translation = ""),
            role = "",
            setting = "",
            steps = emptyList(),
        )

        val checked = content.trimmedIfDraft()

        assertEquals(expected = content, actual = checked)
    }

    @Test
    fun `GIVEN a guest with an empty description WHEN checking the draft THEN it stays a guest`() {
        val content = StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = "  "))

        val checked = content.trimmedIfDraft()

        assertEquals(expected = Interlocutor.Guest(description = ""), actual = checked?.interlocutor)
    }

    @Test
    fun `GIVEN a filled step and an empty step after it WHEN checking the draft THEN only the filled step stays`() {
        val filled = StubTopic.stubStep(task = "Order.")
        val empty = StubTopic.stubStep(outcome = " ", passWhen = "", task = "  ")
        val content = StubTopic.stubContent(steps = listOf(filled, empty))

        val checked = content.trimmedIfDraft()

        assertEquals(expected = listOf(filled), actual = checked?.steps)
    }

    @Test
    fun `GIVEN a step with only an outcome WHEN checking the draft THEN the step stays`() {
        val step = StubTopic.stubStep(outcome = "Done.", passWhen = "", task = "")
        val content = StubTopic.stubContent(steps = listOf(step))

        val checked = content.trimmedIfDraft()

        assertEquals(expected = listOf(step), actual = checked?.steps)
    }

    @Test
    fun `GIVEN a title with spaces only WHEN checking the draft THEN there is no content`() {
        val content = StubTopic.stubContent(title = "   ")

        val checked = content.trimmedIfDraft()

        assertNull(checked)
    }

    @Test
    fun `GIVEN no release date WHEN checking the draft THEN there is no content`() {
        val content = StubTopic.stubContent(releaseDate = null)

        val checked = content.trimmedIfDraft()

        assertNull(checked)
    }

    @Test
    fun `GIVEN a duration of 0 minutes WHEN checking the draft THEN there is no content`() {
        val content = StubTopic.stubContent(durationMinutes = 0)

        val checked = content.trimmedIfDraft()

        assertNull(checked)
    }

    @Test
    fun `GIVEN a duration of 1 minute WHEN checking the draft THEN the draft is valid`() {
        val content = StubTopic.stubContent(durationMinutes = 1)

        val checked = content.trimmedIfDraft()

        assertEquals(expected = content, actual = checked)
    }
}
