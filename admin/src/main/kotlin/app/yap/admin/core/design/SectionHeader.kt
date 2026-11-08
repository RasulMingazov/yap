package app.yap.admin.core.design

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun SectionHeader(title: String) {
    Text(
        text = title,
        color = AdminTheme.Colors.ink,
        style = AdminTheme.Type.sub,
        fontWeight = FontWeight.SemiBold,
    )
}
