package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.characters.model.UpdateCharacterOutcome
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class UpdateCharacterUseCaseTest {

    @Test
    fun `GIVEN a filled profile WHEN updating THEN the character is stored`() = runTest {
        val env = Environment()

        val outcome = env.useCase(StubCharacter.stubCharacter())

        env.characterRepository.updateCall.calledWith(StubCharacter.stubCharacter())
        assertEquals(expected = UpdateCharacterOutcome.Updated(StubCharacter.stubCharacter()), actual = outcome)
    }

    @Test
    fun `GIVEN a profile with an empty field WHEN updating THEN nothing is stored`() = runTest {
        val env = Environment()

        val outcome = env.useCase(StubCharacter.stubCharacter(profile = StubCharacter.stubProfile(personality = "")))

        env.characterRepository.updateCall.notCalled()
        assertEquals(expected = UpdateCharacterOutcome.Invalid, actual = outcome)
    }

    @Test
    fun `GIVEN an unknown character WHEN updating it THEN it is not found`() = runTest {
        val env = Environment()
        env.characterRepository.updateCall.returns(false)

        val outcome = env.useCase(StubCharacter.stubCharacter())

        assertEquals(expected = UpdateCharacterOutcome.NotFound, actual = outcome)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val useCase = UpdateCharacterUseCase(characterRepository = characterRepository)
    }
}
