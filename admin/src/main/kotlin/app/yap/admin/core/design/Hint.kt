package app.yap.admin.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun Hint(text: String) {
    Text(
        text = text,
        color = AdminTheme.Colors.ink,
        style = AdminTheme.Type.body,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = AdminTheme.Colors.fill,
                shape = RoundedCornerShape(AdminTheme.Sizes.cornerRadius),
            )
            .padding(
                horizontal = AdminTheme.Sizes.hintHorizontalPadding,
                vertical = AdminTheme.Sizes.hintVerticalPadding,
            ),
    )
}
