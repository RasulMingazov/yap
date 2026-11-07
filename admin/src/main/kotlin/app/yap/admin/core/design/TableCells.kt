package app.yap.admin.core.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = AdminTheme.Colors.muted,
        style = AdminTheme.Type.small,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        modifier = modifier.padding(
            top = AdminTheme.Sizes.tableHeaderPadding,
            end = AdminTheme.Sizes.tableCellEndPadding,
            bottom = AdminTheme.Sizes.tableHeaderPadding,
        ),
    )
}

@Composable
internal fun TableCell(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.padding(
            top = AdminTheme.Sizes.tableCellPadding,
            end = AdminTheme.Sizes.tableCellEndPadding,
            bottom = AdminTheme.Sizes.tableCellPadding,
        ),
    ) {
        content()
    }
}
