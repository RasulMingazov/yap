package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.CreateTopicOutcome
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.persistence.TopicRepository

class CreateTopicUseCase(
    private val getCharacterUseCase: GetCharacterUseCase,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(content: TopicContent): CreateTopicOutcome {
        if (!content.isDraftReady || isUnknownCharacter(content.interlocutor)) return CreateTopicOutcome.Invalid
        return CreateTopicOutcome.Created(topicRepository.create(content))
    }

    private suspend fun isUnknownCharacter(interlocutor: Interlocutor?): Boolean =
        interlocutor is Interlocutor.Cast && getCharacterUseCase(interlocutor.characterId) == null
}
