package app.yap.admin.feature.topics.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.yap.admin.core.design.AdminTextField
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.LinkButton
import app.yap.admin.core.design.Note
import app.yap.admin.core.design.SectionHeader
import app.yap.admin.feature.topics.domain.Step
import app.yap.admin.feature.topics.presentation.TopicViewModel.Event
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState

@Composable
internal fun StepsSection(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap)) {
        SectionHeader(
            note = "одни для всех уровней",
            number = 4,
            title = "Шаги",
        )
        content.form.steps.forEachIndexed { index, step ->
            StepRow(
                enabled = content.isSaveEnabled,
                number = index + 1,
                onStepChange = { changed -> onEvent(Event.StepChanged(index = index, step = changed)) },
                step = step,
            )
        }
        LinkButton(
            onClick = { onEvent(Event.AddStepClicked) },
            text = "Добавить шаг",
            enabled = content.isSaveEnabled,
        )
        Note(
            text = "Под рейтинг пользователя меняется только речь собеседника и подсказки. " +
                "Шаги и проверка остаются такими, как здесь.",
        )
    }
}

@Composable
private fun StepRow(
    enabled: Boolean,
    number: Int,
    onStepChange: (Step) -> Unit,
    step: Step,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap)) {
        Text(
            text = number.toString(),
            modifier = Modifier
                .width(AdminTheme.Sizes.stepNumberWidth)
                .padding(top = AdminTheme.Sizes.stepNumberTopPadding),
            color = AdminTheme.Colors.muted,
            style = AdminTheme.Type.body,
        )
        AdminTextField(
            label = "Задача на экране",
            onValueChange = { task -> onStepChange(step.copy(task = task)) },
            value = step.task,
            enabled = enabled,
            minLines = STEP_LINES,
            modifier = Modifier.weight(1f),
        )
        AdminTextField(
            label = "Засчитать, если",
            onValueChange = { passWhen -> onStepChange(step.copy(passWhen = passWhen)) },
            value = step.passWhen,
            enabled = enabled,
            minLines = STEP_LINES,
            modifier = Modifier.weight(1f),
        )
        AdminTextField(
            label = "Итог шага",
            onValueChange = { outcome -> onStepChange(step.copy(outcome = outcome)) },
            value = step.outcome,
            enabled = enabled,
            minLines = STEP_LINES,
            modifier = Modifier.weight(1f),
        )
    }
}

private const val STEP_LINES = 2
