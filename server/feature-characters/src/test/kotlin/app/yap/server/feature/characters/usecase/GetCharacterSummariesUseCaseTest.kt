package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.CharacterId
import app.yap.server.feature.characters.model.CharacterSummary
import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid
import kotlinx.coroutines.test.runTest

internal class GetCharacterSummariesUseCaseTest {

    @Test
    fun `GIVEN two characters and topics of one WHEN listing THEN the other has the count 0`() = runTest {
        val sam = StubCharacter.stubCharacter()
        val mia = StubCharacter.stubCharacter(
            characterId = CharacterId(Uuid.parse("5b1e7c90-3d24-4f68-a0b9-6c8d2e4f1a37")),
            profile = StubCharacter.stubProfile(name = "Mia"),
        )
        val env = Environment(characters = listOf(sam, mia), counts = mapOf(sam.characterId to 3))

        val summaries = env.useCase()

        assertEquals(
            expected = listOf(
                CharacterSummary(character = sam, topicCount = 3),
                CharacterSummary(character = mia, topicCount = 0),
            ),
            actual = summaries,
        )
    }

    private class Environment(
        characters: List<Character>,
        counts: Map<CharacterId, Int>,
    ) {

        val characterRepository = StubCharacterRepository().apply { listCall.returns(characters) }
        val characterTopicCounts = StubCharacterTopicCounts(counts = counts)
        val useCase = GetCharacterSummariesUseCase(
            characterRepository = characterRepository,
            characterTopicCounts = characterTopicCounts,
        )
    }
}
