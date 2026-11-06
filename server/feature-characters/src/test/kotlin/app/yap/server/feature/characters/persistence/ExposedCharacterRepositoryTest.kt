package app.yap.server.feature.characters.persistence

import app.yap.server.core.database.withTestDatabase
import app.yap.server.feature.characters.model.StubCharacter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class ExposedCharacterRepositoryTest {

    @Test
    fun `GIVEN a created character WHEN reading it THEN it has the same profile`() = withTestDatabase { database ->
        runTest {
            val repository = ExposedCharacterRepository(database)
            val character = repository.create(StubCharacter.stubProfile())

            val found = repository.find(character.characterId)

            assertEquals(expected = character, actual = found)
        }
    }

    @Test
    fun `GIVEN characters WHEN listing them THEN they come in the order of their names`() = withTestDatabase { database ->
        runTest {
            val repository = ExposedCharacterRepository(database)
            repository.create(StubCharacter.stubProfile(name = "Sam"))
            repository.create(StubCharacter.stubProfile(name = "dan"))
            repository.create(StubCharacter.stubProfile(name = "Mia"))

            val names = repository.list().map { character -> character.profile.name }

            assertEquals(expected = listOf("dan", "Mia", "Sam"), actual = names)
        }
    }

    @Test
    fun `GIVEN a saved character WHEN updating its profile THEN reading it returns the new profile`() =
        withTestDatabase { database ->
            runTest {
                val repository = ExposedCharacterRepository(database)
                val character = repository.create(StubCharacter.stubProfile())
                val changed = character.copy(profile = StubCharacter.stubProfile(name = "Samuel"))

                val updated = repository.update(changed)

                assertEquals(expected = true to changed, actual = updated to repository.find(character.characterId))
            }
        }

    @Test
    fun `GIVEN an unknown character WHEN updating it THEN nothing is stored`() = withTestDatabase { database ->
        runTest {
            val repository = ExposedCharacterRepository(database)

            val updated = repository.update(StubCharacter.stubCharacter())

            assertEquals(expected = false to emptyList(), actual = updated to repository.list())
        }
    }
}
