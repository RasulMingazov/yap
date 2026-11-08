package app.yap.server.feature.conversation.model

import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.topics.model.OpeningLine
import app.yap.server.feature.topics.model.TopicId

internal data class Conversation(
    val currentStep: Int?,
    val level: LevelCode,
    val lines: List<ConversationLine>,
    val topicId: TopicId,
) {

    val lastLine: ConversationLine.Interlocutor
        get() = lines.filterIsInstance<ConversationLine.Interlocutor>().last()

    fun answersOn(step: Int): List<String> = lines
        .filterIsInstance<ConversationLine.Learner>()
        .filter { line -> line.step == step }
        .map { line -> line.text }

    fun answered(
        answer: String,
        verdict: Verdict,
        reply: ConversationLine.Interlocutor,
        stepCount: Int,
    ): Conversation {
        val step = checkNotNull(currentStep) { "A finished conversation takes no answer" }
        val learnerLine = ConversationLine.Learner(
            step = step,
            text = answer,
            verdict = verdict,
        )
        return copy(
            currentStep = nextStep(step, verdict, stepCount),
            lines = lines + learnerLine + reply,
        )
    }

    private fun nextStep(
        step: Int,
        verdict: Verdict,
        stepCount: Int,
    ): Int? = when (verdict) {
        Verdict.ACCEPTED -> (step + 1).takeIf { next -> next <= stepCount }
        Verdict.CONTINUE, Verdict.UNCLEAR, Verdict.NOT_ACCEPTED -> step
    }

    companion object {

        fun start(
            level: LevelCode,
            openingLine: OpeningLine,
            topicId: TopicId,
        ): Conversation = Conversation(
            currentStep = 1,
            level = level,
            lines = listOf(
                ConversationLine.Interlocutor(
                    english = openingLine.english,
                    translation = openingLine.translation,
                ),
            ),
            topicId = topicId,
        )
    }
}
