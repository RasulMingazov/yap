package app.yap.admin.core.design

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.yap.admin.core.presentation.Status

@Composable
internal fun RowScope.ToolbarSaveAction(
    status: Status?,
    button: @Composable () -> Unit,
) {
    status?.let { saveStatus ->
        StatusText(
            status = saveStatus,
            modifier = Modifier.weight(
                weight = 1f,
                fill = false,
            ),
        )
    }
    button()
}
