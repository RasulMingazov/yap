package app.yap.admin.feature.topics.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.core.design.PopupButton
import app.yap.admin.core.design.PrimaryButton
import app.yap.admin.core.design.SecondaryButton
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

@Composable
internal fun ReleaseDateField(
    date: LocalDate?,
    enabled: Boolean,
    onDatePick: (LocalDate) -> Unit,
    text: String?,
) {
    var isPickerOpen by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Дата выхода",
            color = AdminTheme.Colors.muted,
            style = AdminTheme.Type.body,
        )
        PopupButton(
            enabled = enabled,
            onClick = { isPickerOpen = true },
            placeholder = "Выберите дату",
            text = text,
        )
    }
    if (isPickerOpen) {
        ReleaseDatePicker(
            date = date,
            onDatePick = { picked ->
                isPickerOpen = false
                onDatePick(picked)
            },
            onDismiss = { isPickerOpen = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReleaseDatePicker(
    date: LocalDate?,
    onDatePick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = remember {
        DatePickerState(
            locale = RUSSIAN,
            initialSelectedDateMillis = date?.toUtcMillis(),
        )
    }
    val selected = state.selectedDateMillis
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PrimaryButton(
                enabled = selected != null,
                onClick = { selected?.let { millis -> onDatePick(millis.toUtcDate()) } },
                text = "Выбрать",
            )
        },
        dismissButton = {
            SecondaryButton(
                enabled = true,
                onClick = onDismiss,
                text = "Отменить",
            )
        },
    ) {
        DatePicker(
            state = state,
            title = null,
            headline = {
                Text(
                    text = "Дата выхода",
                    color = AdminTheme.Colors.ink,
                    style = AdminTheme.Type.title,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        start = AdminTheme.Sizes.dialogPadding,
                        top = AdminTheme.Sizes.dialogPadding,
                        end = AdminTheme.Sizes.gap,
                        bottom = AdminTheme.Sizes.gap,
                    ),
                )
            },
            showModeToggle = false,
        )
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC)
    .toInstant()
    .toEpochMilli()

private fun Long.toUtcDate(): LocalDate = Instant
    .ofEpochMilli(this)
    .atOffset(ZoneOffset.UTC)
    .toLocalDate()

private val RUSSIAN = Locale.forLanguageTag("ru-RU")
