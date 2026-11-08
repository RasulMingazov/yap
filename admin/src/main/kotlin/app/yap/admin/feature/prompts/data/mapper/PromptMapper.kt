package app.yap.admin.feature.prompts.data.mapper

import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptGroup
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.PromptVersion
import app.yap.contract.admin.PromptDto
import app.yap.contract.admin.PromptSummaryDto
import app.yap.contract.admin.PromptVersionDto
import java.time.Instant

internal fun PromptSummaryDto.toDomain(): PromptSummary = PromptSummary(
    group = PromptGroup.valueOf(group),
    promptKey = PromptKey.valueOf(promptKey),
)

internal fun PromptDto.toDomain(): Prompt = Prompt(
    promptKey = PromptKey.valueOf(promptKey),
    text = text,
    variables = variables,
    versions = versions.map { version -> version.toDomain() },
)

private fun PromptVersionDto.toDomain(): PromptVersion = PromptVersion(
    isCurrent = isCurrent,
    number = number,
    savedAt = Instant.parse(savedAt),
)
