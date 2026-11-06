package app.yap.admin.core.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object AdminTheme {

    object Colors {

        val background = Color(0xFFFFFFFF)
        val ink = Color(0xFF111113)
        val line = Color(0xFFECECEE)
        val muted = Color(0xFF6B6B72)
        val side = Color(0xFFFAFAFA)
    }

    object Sizes {

        val contentPadding = 32.dp
        val fieldWidth = 360.dp
        val gap = 12.dp
        val listMaxWidth = 880.dp
        val rowMinHeight = 48.dp
        val rowPadding = 12.dp
        val sidePadding = 22.dp
        val sideWidth = 220.dp
        val toolbarHeight = 64.dp
    }

    object Type {

        val body = 14.sp
        val small = 12.sp
        val sub = 13.sp
        val title = 17.sp
    }
}
