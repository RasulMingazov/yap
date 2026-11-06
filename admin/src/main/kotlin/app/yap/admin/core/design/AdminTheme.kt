package app.yap.admin.core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object AdminTheme {

    object Colors {

        val background = Color(0xFFFFFFFF)
        val fill = Color(0xFFF4F4F5)
        val ink = Color(0xFF111113)
        val line = Color(0xFFECECEE)
        val muted = Color(0xFF6B6B72)
        val selected = Color(0xFFEDEDF0)
        val side = Color(0xFFFAFAFA)
    }

    object Sizes {

        val contentMaxWidth = 880.dp
        val contentPadding = 32.dp
        val cornerRadius = 6.dp
        val fieldWidth = 360.dp
        val gap = 12.dp
        val hintPadding = 12.dp
        val rowMinHeight = 48.dp
        val rowPadding = 12.dp
        val sideItemPadding = 8.dp
        val sidePadding = 22.dp
        val sideWidth = 220.dp
        val toolbarHeight = 64.dp
        val toolbarTitleGap = 8.dp
    }

    object Type {

        val body = 14.sp
        val small = 12.sp
        val sub = 13.sp
        val title = 17.sp
    }
}
