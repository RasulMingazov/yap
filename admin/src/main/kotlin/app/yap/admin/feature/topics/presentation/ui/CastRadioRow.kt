package app.yap.admin.feature.topics.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState.Content.Choice

@Composable
internal fun CastRadioRow(
    choice: Choice,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AdminTheme.Sizes.rowMinHeight)
            .selectable(
                selected = choice.isSelected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(vertical = AdminTheme.Sizes.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioDot(isSelected = choice.isSelected)
        Text(
            text = choice.name,
            modifier = Modifier.weight(1f),
            color = AdminTheme.Colors.ink,
            style = AdminTheme.Type.body,
        )
        choice.meta?.let { meta ->
            Text(
                text = meta,
                color = AdminTheme.Colors.muted,
                style = AdminTheme.Type.sub,
            )
        }
    }
}

@Composable
private fun RadioDot(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(AdminTheme.Sizes.radio)
            .background(
                color = AdminTheme.Colors.background,
                shape = CircleShape,
            )
            .border(
                width = AdminTheme.Sizes.border,
                color = if (isSelected) AdminTheme.Colors.ink else AdminTheme.Colors.lineStrong,
                shape = CircleShape,
            )
            .padding(AdminTheme.Sizes.radioInset)
            .background(
                color = if (isSelected) AdminTheme.Colors.ink else AdminTheme.Colors.background,
                shape = CircleShape,
            ),
    )
}
