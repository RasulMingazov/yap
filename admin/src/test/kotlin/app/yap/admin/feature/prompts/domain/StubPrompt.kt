package app.yap.admin.feature.prompts.domain

import java.time.Instant

internal object StubPrompt {

    const val NUMBER = 1
    const val SAVED_AT = "2026-11-08T10:15:30Z"
    const val TEXT = "You are {interlocutor}."
    const val VARIABLE = "{interlocutor}"

    fun stubPrompt(
        promptKey: PromptKey = PromptKey.TOPIC_REPLY,
        text: String? = TEXT,
        variables: List<String> = listOf(VARIABLE),
        versions: List<PromptVersion> = listOf(stubPromptVersion()),
    ): Prompt = Prompt(
        promptKey = promptKey,
        text = text,
        variables = variables,
        versions = versions,
    )

    fun stubPromptSummaries(): List<PromptSummary> = listOf(
        stubPromptSummary(promptKey = PromptKey.TOPIC_REPLY),
        stubPromptSummary(promptKey = PromptKey.TOPIC_CHECK),
        stubPromptSummary(promptKey = PromptKey.TOPIC_HINTS),
        stubPromptSummary(promptKey = PromptKey.TOPIC_REVIEW),
        stubPromptSummary(group = PromptGroup.CALIBRATION, promptKey = PromptKey.CALIBRATION_CHECK),
        stubPromptSummary(group = PromptGroup.CALIBRATION, promptKey = PromptKey.CALIBRATION_REPLY),
    )

    fun stubPromptSummary(
        group: PromptGroup = PromptGroup.TOPIC,
        promptKey: PromptKey = PromptKey.TOPIC_REPLY,
    ): PromptSummary = PromptSummary(
        group = group,
        promptKey = promptKey,
    )

    fun stubPromptVersion(
        isCurrent: Boolean = true,
        number: Int = NUMBER,
        savedAt: Instant = Instant.parse(SAVED_AT),
    ): PromptVersion = PromptVersion(
        isCurrent = isCurrent,
        number = number,
        savedAt = savedAt,
    )
}
