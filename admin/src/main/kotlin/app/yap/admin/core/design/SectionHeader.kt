package app.yap.admin.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
        modifier = if (note == null) Modifier else Modifier.fillMaxWidth(),
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
            color = AdminTheme.Colors.ink,
            style = AdminTheme.Type.sub,
            fontWeight = FontWeight.SemiBold,
        )
        note?.let { sectionNote ->
            Spacer(modifier = Modifier.weight(1f))
            Note(text = sectionNote)
        }
    }
}
