package app.yap.server.feature.topics.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class TopicContentTest {

    @Test
    fun `GIVEN texts with spaces around WHEN normalizing THEN every text is trimmed`() {
        val content = StubTopic.stubContent(
            description = " Order a coffee. ",
            interlocutor = Interlocutor.Guest(description = " A tourist. "),
            openingLine = StubTopic.stubOpeningLine(english = " Hello! ", translation = " Привет! "),
            role = " A barista. ",
            setting = " A cafe. ",
            steps = listOf(StubTopic.stubStep(outcome = " Ordered. ", passWhen = " Names it. ", task = " Order. ")),
            title = " At the cafe ",
        )

        val normalized = content.normalized()

        assertEquals(
            expected = StubTopic.stubContent(
                description = "Order a coffee.",
                interlocutor = Interlocutor.Guest(description = "A tourist."),
                openingLine = StubTopic.stubOpeningLine(english = "Hello!", translation = "Привет!"),
                role = "A barista.",
                setting = "A cafe.",
                steps = listOf(StubTopic.stubStep(outcome = "Ordered.", passWhen = "Names it.", task = "Order.")),
                title = "At the cafe",
            ),
            actual = normalized,
        )
    }

    @Test
    fun `GIVEN a filled step and an empty step after it WHEN normalizing THEN only the filled step stays`() {
        val content = StubTopic.stubContent(
            steps = listOf(
                StubTopic.stubStep(),
                StubTopic.stubStep(outcome = "  ", passWhen = "", task = " "),
            ),
        )

        val normalized = content.normalized()

        assertEquals(expected = listOf(StubTopic.stubStep()), actual = normalized.steps)
    }

    @Test
    fun `GIVEN a guest with a description of spaces WHEN normalizing THEN the guest stays with no text`() {
        val content = StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = "   "))

        val normalized = content.normalized()

        assertEquals(expected = Interlocutor.Guest(description = ""), actual = normalized.interlocutor)
    }
}
