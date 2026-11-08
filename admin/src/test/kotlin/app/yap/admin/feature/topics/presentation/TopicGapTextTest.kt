package app.yap.admin.feature.topics.presentation

import app.yap.admin.feature.topics.domain.TopicField
import app.yap.admin.feature.topics.domain.TopicGap
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TopicGapTextTest {

    @Test
    fun `GIVEN every field missing WHEN naming THEN each field is named in form order`() {
        val gaps = TopicField.entries.map { field -> TopicGap.Missing(field = field) }

        val text = TopicGapText(gaps)

        assertEquals(
            expected = "название, дату выхода, длительность, описание, обстановку, " +
                "собеседника или описание гостя, роль, " +
                "первую реплику, перевод и шаги",
            actual = text,
        )
    }

    @Test
    fun `GIVEN the steps 3, 4 and 5 incomplete WHEN naming THEN the steps are named as 3–5`() {
        val gaps = listOf(TopicGap.IncompleteSteps(numbers = listOf(3, 4, 5)))

        val text = TopicGapText(gaps)

        assertEquals(expected = "шаги 3–5", actual = text)
    }

    @Test
    fun `GIVEN one incomplete step WHEN naming THEN the step is named alone`() {
        val gaps = listOf(TopicGap.IncompleteSteps(numbers = listOf(2)))

        val text = TopicGapText(gaps)

        assertEquals(expected = "шаг 2", actual = text)
    }

    @Test
    fun `GIVEN the steps 1, 3 and 4 incomplete WHEN naming THEN step 1 stands apart from 3–4`() {
        val gaps = listOf(TopicGap.IncompleteSteps(numbers = listOf(1, 3, 4)))

        val text = TopicGapText(gaps)

        assertEquals(expected = "шаги 1, 3–4", actual = text)
    }

    @Test
    fun `GIVEN a past date and a taken date WHEN naming THEN each asks for another date`() {
        val gaps = listOf(TopicGap.PastDate, TopicGap.DateTaken)

        val text = TopicGapText(gaps)

        assertEquals(expected = "новую дату выхода и другую дату выхода", actual = text)
    }
}
