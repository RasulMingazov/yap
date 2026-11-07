package app.yap.server.feature.prompts.model

import java.time.Instant

data class PromptVersion(
    val number: Int,
    val savedAt: Instant,
    val text: String,
)
