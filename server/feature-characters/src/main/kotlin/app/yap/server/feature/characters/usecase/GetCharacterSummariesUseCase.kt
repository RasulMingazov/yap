package app.yap.server.feature.characters.usecase

import app.yap.server.feature.characters.model.CharacterSummary
import app.yap.server.feature.characters.persistence.CharacterRepository

class GetCharacterSummariesUseCase(
    private val characterRepository: CharacterRepository,
    private val characterTopicCounts: CharacterTopicCounts,
) {

    suspend operator fun invoke(): List<CharacterSummary> {
        val counts = characterTopicCounts.byCharacter()
        return characterRepository.list().map { character ->
            CharacterSummary(
                character = character,
                topicCount = counts[character.characterId] ?: 0,
            )
        }
    }
}
