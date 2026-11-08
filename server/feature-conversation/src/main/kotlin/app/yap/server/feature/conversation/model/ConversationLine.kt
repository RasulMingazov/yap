package app.yap.server.feature.conversation.model

internal sealed interface ConversationLine {

    data class Interlocutor(
        val english: String,
        val translation: String,
    ) : ConversationLine

    data class Learner(
        val step: Int,
        val text: String,
        val verdict: Verdict,
    ) : ConversationLine
}
