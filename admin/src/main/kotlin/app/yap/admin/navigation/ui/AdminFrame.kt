package app.yap.admin.navigation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.yap.admin.core.design.AdminTheme
import app.yap.admin.navigation.NavigationViewModel.Screen

@Composable
internal fun AdminFrame(
    current: Screen,
    onScreenClick: (Screen) -> Unit,
    serverLabel: String,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminTheme.Colors.background),
    ) {
        Sidebar(
            current = current,
            onScreenClick = onScreenClick,
            serverLabel = serverLabel,
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}
