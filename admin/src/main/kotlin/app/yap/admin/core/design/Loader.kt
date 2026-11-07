package app.yap.admin.core.design

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun Loader() {
    CircularProgressIndicator(
        modifier = Modifier.size(AdminTheme.Sizes.loader),
        color = AdminTheme.Colors.ink,
        strokeWidth = AdminTheme.Sizes.loaderStroke,
    )
}
