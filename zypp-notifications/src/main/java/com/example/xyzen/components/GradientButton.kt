package com.example.xyzen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.xyzen.ui.theme.Primary
import com.example.xyzen.ui.theme.PrimaryLight

@Composable
fun GradientButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true
) {
	val gradient = Brush.horizontalGradient(
		colors = listOf(Primary, PrimaryLight)
	)

	Box(
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.drawBehind {
				drawRect(
					brush = if (enabled) gradient else SolidColor(Color.Gray.copy(alpha = 0.3f))
				)
			}
			.clickable(enabled = enabled) { onClick() }
			.padding(horizontal = 16.dp, vertical = 12.dp),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = text,
			color = Color.White,
			style = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.SemiBold
		)
	}
}