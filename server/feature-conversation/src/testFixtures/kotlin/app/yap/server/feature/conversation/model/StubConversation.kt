package app.yap.server.feature.conversation.model

import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicId

internal object StubConversation {

    const val ANSWER = "A coffee, please."
    const val REPLY_ENGLISH = "Sure. What size?"
    const val REPLY_TRANSLATION = "Конечно. Какой размер?"

    fun stubConversation(
        currentStep: Int? = 1,
        level: LevelCode = LevelCode.B1,
        lines: List<ConversationLine> = listOf(stubOpeningInterlocutor()),
        topicId: TopicId = StubTopic.TOPIC_ID,
    ): Conversation = Conversation(
        currentStep = currentStep,
        level = level,
        lines = lines,
        topicId = topicId,
    )

    fun stubOpeningInterlocutor(): ConversationLine.Interlocutor = stubInterlocutor(
        english = StubTopic.ENGLISH,
        translation = StubTopic.TRANSLATION,
    )

    fun stubInterlocutor(
        english: String = REPLY_ENGLISH,
        translation: String = REPLY_TRANSLATION,
    ): ConversationLine.Interlocutor = ConversationLine.Interlocutor(
        english = english,
        translation = translation,
    )

    fun stubLearner(
        step: Int = 1,
        text: String = ANSWER,
        verdict: Verdict = Verdict.CONTINUE,
    ): ConversationLine.Learner = ConversationLine.Learner(
        step = step,
        text = text,
        verdict = verdict,
    )
}
