package app.yap.server.feature.models.persistence

import app.yap.server.core.database.withTestDatabase
import app.yap.server.feature.models.model.ModelTask
import app.yap.server.feature.models.model.StubTaskModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.insert

internal class DatabaseTaskModelRepositoryTest {

    @Test
    fun `GIVEN a clean database WHEN reading the task models THEN there are none`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTaskModelRepository(database)

            val models = repository.list()

            assertEquals(expected = emptyList(), actual = models)
        }
    }

    @Test
    fun `GIVEN a saved table WHEN reading the task models THEN they have the saved values`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTaskModelRepository(database)
            val table = StubTaskModel.stubTable(review = StubTaskModel.stubChoice())
            repository.save(table)

            val models = repository.list()

            assertEquals(expected = table.toSet(), actual = models.toSet())
        }
    }

    @Test
    fun `GIVEN rows of an unknown provider and an unknown task WHEN reading THEN only the known rows are read`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTaskModelRepository(database)
                val known = StubTaskModel.stubTaskModel(task = ModelTask.TOPIC_CHECK)
                repository.save(listOf(known))
                database.transaction {
                    TaskModelsTable.insert { statement -> statement.writeRaw(task = "TOPIC_REVIEW", provider = "OLD") }
                    TaskModelsTable.insert { statement -> statement.writeRaw(task = "OLD", provider = "DEEPSEEK") }
                }

                val models = repository.list()

                assertEquals(expected = listOf(known), actual = models)
            }
        }

    @Test
    fun `GIVEN a saved table WHEN saving again THEN reading returns the new values`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTaskModelRepository(database)
            repository.save(StubTaskModel.stubTable())
            val changed = StubTaskModel.stubTable(review = StubTaskModel.stubChoice())

            repository.save(changed)

            assertEquals(expected = changed.toSet(), actual = repository.list().toSet())
        }
    }
}

private fun UpdateBuilder<*>.writeRaw(task: String, provider: String) {
    this[TaskModelsTable.model] = "deepseek-flash"
    this[TaskModelsTable.provider] = provider
    this[TaskModelsTable.task] = task
}
