package app.yap.admin.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun LoadError(
    message: String,
    onRetryClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap)) {
        Text(
            text = message,
            color = AdminTheme.Colors.ink,
            style = AdminTheme.Type.body,
        )
        OutlinedButton(onClick = onRetryClick) {
            Text("Повторить")
        }
    }
}
