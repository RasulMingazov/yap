package app.yap.server.feature.prompts.model

import java.time.Instant

object StubPrompt {

    const val NUMBER = 1
    const val TEXT = "Play {interlocutor} in {topic.setting}."
    val SAVED_AT: Instant = Instant.parse("2026-10-24T09:30:00Z")

    fun stubPrompt(
        current: PromptVersion? = stubVersion(),
        promptKey: PromptKey = PromptKey.TOPIC_REPLY,
        versions: List<PromptVersion> = listOf(stubVersion()),
    ): Prompt = Prompt(
        current = current,
        promptKey = promptKey,
        versions = versions,
    )

    fun stubVersion(
        number: Int = NUMBER,
        savedAt: Instant = SAVED_AT,
        text: String = TEXT,
    ): PromptVersion = PromptVersion(
        number = number,
        savedAt = savedAt,
        text = text,
    )
}
