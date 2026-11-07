package app.yap.admin.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ScreenBody(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .widthIn(max = AdminTheme.Sizes.contentMaxWidth)
            .verticalScroll(rememberScrollState())
            .padding(
                start = AdminTheme.Sizes.contentPadding,
                top = AdminTheme.Sizes.contentTopPadding,
                end = AdminTheme.Sizes.contentPadding,
                bottom = AdminTheme.Sizes.contentBottomPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.gap),
        content = content,
    )
}
