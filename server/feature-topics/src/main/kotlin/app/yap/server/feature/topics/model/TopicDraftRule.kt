package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.usecase.GetCharacterUseCase

class TopicDraftRule(
    private val getCharacterUseCase: GetCharacterUseCase,
) {

    suspend fun accepts(content: TopicContent): Boolean =
        content.isDraftReady && !isUnknownCharacter(content.interlocutor)

    private suspend fun isUnknownCharacter(interlocutor: Interlocutor?): Boolean =
        interlocutor is Interlocutor.Cast && getCharacterUseCase(interlocutor.characterId) == null
}
