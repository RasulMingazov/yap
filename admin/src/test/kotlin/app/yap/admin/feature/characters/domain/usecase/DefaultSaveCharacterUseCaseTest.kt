package app.yap.admin.feature.characters.domain.usecase

import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.characters.domain.repository.StubCharacterRepository
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

internal class DefaultSaveCharacterUseCaseTest {

    @Test
    fun `GIVEN a new character WHEN saving it THEN the profile is created`() = runTest {
        val env = Environment()

        env.useCase(characterId = null, profile = StubCharacter.stubProfile())

        env.characterRepository.createCall.calledWith(StubCharacter.stubProfile())
    }

    @Test
    fun `GIVEN a saved character WHEN saving it THEN the character is updated`() = runTest {
        val env = Environment()
        val character = StubCharacter.stubCharacter()

        env.useCase(characterId = character.characterId, profile = character.profile)

        env.characterRepository.updateCall.calledWith(character)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val useCase = DefaultSaveCharacterUseCase(characterRepository = characterRepository)
    }
}
