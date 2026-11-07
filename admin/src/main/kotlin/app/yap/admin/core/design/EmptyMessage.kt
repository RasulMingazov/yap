package app.yap.admin.core.design

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun EmptyMessage(text: String) {
    Text(
        text = text,
        color = AdminTheme.Colors.muted,
        style = AdminTheme.Type.body,
    )
}
