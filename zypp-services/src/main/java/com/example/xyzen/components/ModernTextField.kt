package com.example.xyzen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.xyzen.ui.theme.TextHint

@Composable
fun ModernTextField(
	value: String,
	onValueChange: (String) -> Unit,
	label: String,
	modifier: Modifier = Modifier,
	leadingIcon: ImageVector? = null,
	trailingIcon: @Composable (() -> Unit)? = null,
	isError: Boolean = false,
	errorMessage: String? = null,
	keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
	singleLine: Boolean = true,
	visualTransformation: VisualTransformation = VisualTransformation.None,
	maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
	minLines: Int = 1,
) {
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.background(Color.White.copy(alpha = 0.8f))
			.padding(horizontal = 4.dp, vertical = 2.dp)
	) {
		OutlinedTextField(
			value = value,
			onValueChange = onValueChange,
			label = { Text(text = label, color = TextHint) },
			modifier = Modifier.fillMaxWidth(),
			leadingIcon = leadingIcon?.let {
				{ Icon(imageVector = it, contentDescription = null) }
			},
			trailingIcon = trailingIcon,
			isError = isError,
			keyboardOptions = keyboardOptions,
			singleLine = singleLine,
			colors = OutlinedTextFieldDefaults.colors(
				focusedBorderColor = MaterialTheme.colorScheme.primary,
				unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
				errorBorderColor = MaterialTheme.colorScheme.error,
				focusedContainerColor = Color.Transparent,
				unfocusedContainerColor = Color.Transparent
			),
			shape = RoundedCornerShape(10.dp),
			visualTransformation = visualTransformation,
			minLines = minLines,
			maxLines = maxLines,
		)
	}

	if (isError && errorMessage != null) {
		Text(
			text = errorMessage,
			color = MaterialTheme.colorScheme.error,
			style = MaterialTheme.typography.bodySmall,
			modifier = Modifier.padding(start = 16.dp, top = 4.dp)
		)
	}
}