package app.yap.server.feature.models.model

import app.yap.server.core.llm.ModelCatalog
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TaskModelChoiceTest {

    @Test
    fun `GIVEN no stored row for the task WHEN choosing THEN the default comes`() {
        assertEquals(expected = ModelCatalog.DEFAULT, actual = TaskModelChoice.of(emptyList(), ModelTask.TOPIC_REVIEW))
    }

    @Test
    fun `GIVEN a stored row in the catalog WHEN choosing THEN the stored choice comes`() {
        val stored = StubTaskModel.stubTable(review = StubTaskModel.stubChoice())

        val choice = TaskModelChoice.of(stored, ModelTask.TOPIC_REVIEW)

        assertEquals(expected = StubTaskModel.stubChoice(), actual = choice)
    }

    @Test
    fun `GIVEN a stored row outside the catalog WHEN choosing THEN the default comes`() {
        val stored = StubTaskModel.stubTable(review = StubTaskModel.stubChoice(model = "deepseek-chat"))

        val choice = TaskModelChoice.of(stored, ModelTask.TOPIC_REVIEW)

        assertEquals(expected = ModelCatalog.DEFAULT, actual = choice)
    }
}
