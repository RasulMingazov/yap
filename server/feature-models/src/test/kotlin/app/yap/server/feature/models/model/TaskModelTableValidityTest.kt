package app.yap.server.feature.models.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TaskModelTableValidityTest {

    @Test
    fun `GIVEN every task once with a model of its provider WHEN checking the table THEN it is valid`() {
        val table = StubTaskModel.stubTable(review = StubTaskModel.stubChoice())

        assertTrue(TaskModelTableValidity.hasEveryTaskOnce(table))
        assertTrue(TaskModelTableValidity.isInCatalog(table))
    }

    @Test
    fun `GIVEN a table without Hints WHEN checking the tasks THEN the table does not have every task once`() {
        val table = StubTaskModel.stubTable().filter { model -> model.task != ModelTask.TOPIC_HINTS }

        assertFalse(TaskModelTableValidity.hasEveryTaskOnce(table))
    }

    @Test
    fun `GIVEN Review twice WHEN checking the tasks THEN the table does not have every task once`() {
        val table = StubTaskModel.stubTable() + StubTaskModel.stubTaskModel(task = ModelTask.TOPIC_REVIEW)

        assertFalse(TaskModelTableValidity.hasEveryTaskOnce(table))
    }

    @Test
    fun `GIVEN a model that the catalog does not have WHEN checking the catalog THEN the table is not in it`() {
        val table = StubTaskModel.stubTable(review = StubTaskModel.stubChoice(model = "deepseek-chat"))

        assertFalse(TaskModelTableValidity.isInCatalog(table))
    }
}
