package app.yap.server.feature.models.persistence

import app.yap.server.core.database.withTestDatabase
import app.yap.server.feature.models.model.StubTaskModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

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
