package app.yap.admin.feature.topics.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.yap.admin.core.design.AdminTextField
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.Note
import app.yap.admin.core.design.SectionHeader
import app.yap.admin.feature.topics.presentation.TopicViewModel.Event
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState

@Composable
internal fun InterlocutorSection(
    content: UiState.Content,
    onEvent: (Event) -> Unit,
) {
    val form = content.form
    Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap)) {
        SectionHeader(
            number = 2,
            title = "Собеседник",
        )
        content.castNote?.let { note -> Note(text = note) }
        Column(modifier = Modifier.selectableGroup()) {
            HorizontalDivider(color = AdminTheme.Colors.line)
            content.choices.forEach { choice ->
                CastRadioRow(
                    choice = choice,
                    enabled = content.isSaveEnabled,
                    onClick = { onEvent(Event.InterlocutorChosen(choice.interlocutor)) },
                )
                HorizontalDivider(color = AdminTheme.Colors.line)
            }
        }
        content.guestDescription?.let { description ->
            AdminTextField(
                label = "Какой он, этот гость",
                onValueChange = { text -> onEvent(Event.GuestDescriptionChanged(text)) },
                value = description,
                enabled = content.isSaveEnabled,
                minLines = GUEST_LINES,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AdminTextField(
            label = "Роль в сцене — подпись над репликами",
            onValueChange = { role -> onEvent(Event.FormChanged(form.copy(role = role))) },
            value = form.role,
            enabled = content.isSaveEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private const val GUEST_LINES = 2
