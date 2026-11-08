package app.yap.admin.feature.prompts.domain

import java.time.Instant

internal data class PromptVersion(
    val isCurrent: Boolean,
    val number: Int,
    val savedAt: Instant,
)
