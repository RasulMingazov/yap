package app.yap.admin.feature.models.presentation

import app.yap.admin.feature.models.domain.ModelTask
import app.yap.admin.feature.prompts.domain.PromptGroup
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.presentation.title
import app.yap.admin.feature.prompts.presentation.usage

internal fun ModelTask.title(): String = when (this) {
    ModelTask.CALIBRATION -> PromptGroup.CALIBRATION.title()
    ModelTask.TOPIC_CHECK -> PromptKey.TOPIC_CHECK.title()
    ModelTask.TOPIC_HINTS -> PromptKey.TOPIC_HINTS.title()
    ModelTask.TOPIC_REPLY -> PromptKey.TOPIC_REPLY.title()
    ModelTask.TOPIC_REVIEW -> PromptKey.TOPIC_REVIEW.title()
}

internal fun ModelTask.usage(): String = when (this) {
    ModelTask.CALIBRATION -> "проверка и реплики Сэма"
    ModelTask.TOPIC_CHECK -> PromptKey.TOPIC_CHECK.usage()
    ModelTask.TOPIC_HINTS -> PromptKey.TOPIC_HINTS.usage()
    ModelTask.TOPIC_REPLY -> PromptKey.TOPIC_REPLY.usage()
    ModelTask.TOPIC_REVIEW -> PromptKey.TOPIC_REVIEW.usage()
}
