package com.example.xyzen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NeumorphicCard(
	modifier: Modifier = Modifier,
	cornerRadius: Int = 16,
	elevation: Int = 8,
	content: @Composable () -> Unit
) {
	Box(
		modifier = modifier
			.shadow(
				elevation = elevation.dp,
				shape = RoundedCornerShape(cornerRadius.dp),
				spotColor = Color.Black.copy(alpha = 0.1f),
				ambientColor = Color.Black.copy(alpha = 0.05f)
			)
			.clip(RoundedCornerShape(cornerRadius.dp))
			.background(MaterialTheme.colorScheme.surface)
			.padding(16.dp)
	) {
		content()
	}
}