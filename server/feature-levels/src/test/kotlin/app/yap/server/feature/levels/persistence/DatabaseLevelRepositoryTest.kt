package app.yap.server.feature.levels.persistence

import app.yap.server.core.database.withTestDatabase
import app.yap.server.feature.levels.model.Idioms
import app.yap.server.feature.levels.model.StubLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class DatabaseLevelRepositoryTest {

    @Test
    fun `GIVEN a clean database WHEN reading the levels THEN there are none`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseLevelRepository(database)

            val entries = repository.list()

            assertEquals(expected = emptyList(), actual = entries)
        }
    }

    @Test
    fun `GIVEN a saved table WHEN reading the levels THEN they have the saved values`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseLevelRepository(database)
            val table = StubLevel.stubTable()
            repository.save(table)

            val entries = repository.list()

            assertEquals(expected = table.toSet(), actual = entries.toSet())
        }
    }

    @Test
    fun `GIVEN a saved table WHEN saving again THEN reading returns the new values`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseLevelRepository(database)
            repository.save(StubLevel.stubTable())
            val changed = StubLevel.stubTable(
                b1 = StubLevel.stubSettings(
                    block = "Speak naturally.",
                    idioms = Idioms.FREELY,
                    maxWords = 24,
                    ratingFrom = 1100,
                    vocabulary = "B2",
                ),
            )

            repository.save(changed)

            assertEquals(expected = changed.toSet(), actual = repository.list().toSet())
        }
    }

    @Test
    fun `GIVEN a level saved with a model WHEN reading the levels THEN it has the model`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseLevelRepository(database)
            val table = StubLevel.stubTable(b1 = StubLevel.stubSettings(model = StubLevel.MODEL))
            repository.save(table)

            val entries = repository.list()

            assertEquals(expected = table.toSet(), actual = entries.toSet())
        }
    }

    @Test
    fun `GIVEN a level with a model WHEN saving it without one THEN it has no model`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseLevelRepository(database)
            repository.save(StubLevel.stubTable(b1 = StubLevel.stubSettings(model = StubLevel.MODEL)))
            val withoutModel = StubLevel.stubTable()

            repository.save(withoutModel)

            assertEquals(expected = withoutModel.toSet(), actual = repository.list().toSet())
        }
    }

    @Test
    fun `GIVEN a stored model outside the catalog WHEN reading the levels THEN the level has no model`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseLevelRepository(database)
                repository.save(StubLevel.stubTable(b1 = StubLevel.stubSettings(model = "deepseek-chat")))

                val entries = repository.list()

                assertEquals(expected = StubLevel.stubTable().toSet(), actual = entries.toSet())
            }
        }
}
