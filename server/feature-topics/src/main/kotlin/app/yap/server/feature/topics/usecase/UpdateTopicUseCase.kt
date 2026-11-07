package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.UpdateTopicOutcome
import app.yap.server.feature.topics.persistence.TopicRepository

class UpdateTopicUseCase(
    private val getCharacterUseCase: GetCharacterUseCase,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(topic: Topic): UpdateTopicOutcome {
        val content = topic.content
        if (!content.isDraftReady || isUnknownCharacter(content.interlocutor)) return UpdateTopicOutcome.Invalid
        return if (topicRepository.update(topic)) {
            UpdateTopicOutcome.Updated(topic)
        } else {
            UpdateTopicOutcome.NotFound
        }
    }

    private suspend fun isUnknownCharacter(interlocutor: Interlocutor?): Boolean =
        interlocutor is Interlocutor.Cast && getCharacterUseCase(interlocutor.characterId) == null
}
