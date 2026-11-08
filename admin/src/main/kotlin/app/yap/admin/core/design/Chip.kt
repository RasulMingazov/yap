package app.yap.admin.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
internal fun Chip(text: String) {
    Text(
        text = text,
        color = AdminTheme.Colors.ink,
        style = AdminTheme.Type.mono,
        modifier = Modifier
            .height(AdminTheme.Sizes.chipHeight)
            .clip(RoundedCornerShape(AdminTheme.Sizes.chipRadius))
            .background(AdminTheme.Colors.fill)
            .padding(horizontal = AdminTheme.Sizes.chipPadding)
            .wrapContentHeight(Alignment.CenterVertically),
    )
}
