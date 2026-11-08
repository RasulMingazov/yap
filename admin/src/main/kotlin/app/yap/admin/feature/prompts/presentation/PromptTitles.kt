package app.yap.admin.feature.prompts.presentation

import app.yap.admin.feature.prompts.domain.PromptGroup
import app.yap.admin.feature.prompts.domain.PromptKey

internal fun PromptGroup.title(): String = when (this) {
    PromptGroup.CALIBRATION -> "Калибровка"
    PromptGroup.TOPIC -> "Тема"
}

internal fun PromptKey.title(): String = when (this) {
    PromptKey.CALIBRATION_CHECK -> "Проверка ответа"
    PromptKey.CALIBRATION_REPLY -> "Реплика Сэма"
    PromptKey.TOPIC_CHECK -> "Проверка шага"
    PromptKey.TOPIC_HINTS -> "Подсказки"
    PromptKey.TOPIC_REPLY -> "Реплика персонажа"
    PromptKey.TOPIC_REVIEW -> "Разбор и рекомендации"
}

internal fun PromptKey.usage(): String = when (this) {
    PromptKey.CALIBRATION_CHECK -> "каждый ответ калибровки"
    PromptKey.CALIBRATION_REPLY -> "после каждого ответа калибровки"
    PromptKey.TOPIC_CHECK -> "каждый ответ"
    PromptKey.TOPIC_HINTS -> "по запросу пользователя"
    PromptKey.TOPIC_REPLY -> "после каждого ответа"
    PromptKey.TOPIC_REVIEW -> "в конце темы"
}
