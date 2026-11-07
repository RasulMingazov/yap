package app.yap.server.feature.prompts.api

import app.yap.contract.admin.PromptDto
import app.yap.contract.admin.PromptSummaryDto
import app.yap.contract.admin.PromptTextDto
import app.yap.contract.admin.PromptVersionDto
import app.yap.server.feature.prompts.model.Prompt
import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.model.PromptVersion

internal fun PromptKey.toSummaryDto(): PromptSummaryDto = PromptSummaryDto(
    group = group.name,
    promptKey = name,
)

internal fun Prompt.toDto(): PromptDto = PromptDto(
    group = promptKey.group.name,
    promptKey = promptKey.name,
    text = current?.text,
    variables = promptKey.variables,
    versions = versions.map { version -> version.toDto(isCurrent = version == current) },
)

private fun PromptVersion.toDto(isCurrent: Boolean): PromptVersionDto = PromptVersionDto(
    isCurrent = isCurrent,
    number = number,
    savedAt = savedAt.toString(),
)

internal fun PromptTextDto.toText(): String = text.trim()
