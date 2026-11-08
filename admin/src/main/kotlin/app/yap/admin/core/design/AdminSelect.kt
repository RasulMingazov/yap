package app.yap.admin.core.design

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal fun <T> AdminSelect(
    enabled: Boolean,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    options: List<T>,
    placeholder: String,
    value: T?,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box {
        PopupButton(
            enabled = enabled,
            onClick = { isExpanded = true },
            placeholder = placeholder,
            text = value?.let(label),
            height = AdminTheme.Sizes.cellFieldHeight,
        )
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label(option),
                            color = AdminTheme.Colors.ink,
                            style = AdminTheme.Type.body,
                        )
                    },
                    onClick = {
                        isExpanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}
