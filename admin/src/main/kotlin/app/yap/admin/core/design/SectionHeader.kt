package app.yap.admin.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun SectionHeader(
    title: String,
    note: String? = null,
    number: Int? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AdminTheme.Sizes.sectionHeaderGap),
    ) {
        number?.let { sectionNumber ->
            Text(
                text = sectionNumber.toString(),
                modifier = Modifier.width(AdminTheme.Sizes.sectionNumberWidth),
                color = AdminTheme.Colors.muted,
                style = AdminTheme.Type.sub,
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = AdminTheme.Colors.ink,
            style = AdminTheme.Type.sub,
            fontWeight = FontWeight.SemiBold,
        )
        note?.let { sectionNote -> Note(text = sectionNote) }
    }
}
