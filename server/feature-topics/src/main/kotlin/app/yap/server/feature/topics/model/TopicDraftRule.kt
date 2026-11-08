package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.usecase.GetCharacterUseCase

class TopicDraftRule(
    private val getCharacterUseCase: GetCharacterUseCase,
) {

    suspend fun gaps(content: TopicContent): Set<TopicGap> = buildSet {
        if (content.title.isEmpty()) add(TopicGap.Missing(TopicField.TITLE))
        if (content.releaseDate == null) add(TopicGap.Missing(TopicField.RELEASE_DATE))
        if (content.durationMinutes != null && content.durationMinutes < 1) add(TopicGap.Missing(TopicField.DURATION))
        if (isUnknownCharacter(content.interlocutor)) add(TopicGap.Missing(TopicField.INTERLOCUTOR))
    }

    private suspend fun isUnknownCharacter(interlocutor: Interlocutor?): Boolean =
        interlocutor is Interlocutor.Cast && getCharacterUseCase(interlocutor.characterId) == null
}
