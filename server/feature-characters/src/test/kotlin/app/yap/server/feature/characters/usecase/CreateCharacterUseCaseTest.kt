package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.CreateCharacterOutcome
import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class CreateCharacterUseCaseTest {

    @Test
    fun `GIVEN a filled profile WHEN creating THEN the profile is stored`() = runTest {
        val env = Environment()

        val outcome = env.useCase(StubCharacter.stubProfile())

        env.characterRepository.createCall.calledWith(StubCharacter.stubProfile())
        assertEquals(expected = CreateCharacterOutcome.Created(StubCharacter.stubCharacter()), actual = outcome)
    }

    @Test
    fun `GIVEN a profile with an empty field WHEN creating THEN nothing is stored`() = runTest {
        val env = Environment()

        val outcome = env.useCase(StubCharacter.stubProfile(history = ""))

        env.characterRepository.createCall.notCalled()
        assertEquals(expected = CreateCharacterOutcome.Invalid, actual = outcome)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val useCase = CreateCharacterUseCase(characterRepository = characterRepository)
    }
}
