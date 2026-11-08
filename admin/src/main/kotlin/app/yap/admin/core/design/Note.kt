package app.yap.admin.core.design

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString

@Composable
internal fun Note(text: String) {
    Note(text = AnnotatedString(text))
}

@Composable
internal fun Note(text: AnnotatedString) {
    Text(
        text = text,
        color = AdminTheme.Colors.muted,
        style = AdminTheme.Type.sub,
    )
}
