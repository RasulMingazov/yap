package app.yap.admin.core.design

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
internal fun AdminTextField(
    label: String,
    onValueChange: (String) -> Unit,
    value: String,
    enabled: Boolean = true,
    minLines: Int = 1,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    secret: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        textStyle = textStyle,
        label = { Text(label) },
        placeholder = placeholder?.let { hint -> { Text(hint) } },
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = minLines == 1,
        minLines = minLines,
    )
}
