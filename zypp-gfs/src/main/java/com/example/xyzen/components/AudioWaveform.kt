package com.example.xyzen.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AudioWaveform(
	modifier: Modifier = Modifier,
	barCount: Int = 30,
	color: Color = Color.White.copy(alpha = 0.5f),
	isPlaying: Boolean = true
) {
	// Create infinite animations with different phases for each bar
	val infiniteTransition = rememberInfiniteTransition(label = "waveform")

	val animatedValues = List(barCount) { i ->
		val animationPhase = i * (360f / barCount)
		infiniteTransition.animateFloat(
			initialValue = 0f,
			targetValue = 360f,
			animationSpec = infiniteRepeatable(
				animation = tween(2000, easing = LinearEasing),
				repeatMode = RepeatMode.Restart
			),
			label = "bar$i"
		).value
	}

	Canvas(
		modifier = modifier
			.fillMaxWidth()
			.height(60.dp)
	) {
		val barWidth = size.width / (barCount * 2)
		val maxBarHeight = size.height * 0.8f

		for (i in 0 until barCount) {
			// Create different animation phases for each bar
			val animationPhase = i * (360f / barCount)

			val animatedValue = animatedValues[i]

			// Calculate height based on sine wave with phase offset
			val phase = (animatedValue + animationPhase) % 360
			val heightPercentage = if (isPlaying) {
				// Use sine wave for smooth animation
				(sin(Math.toRadians(phase.toDouble())) * 0.5 + 0.5).toFloat() * 0.8f + 0.2f
			} else {
				// When not playing, show a flat line with small variations
				0.3f
			}

			val barHeight = maxBarHeight * heightPercentage
			val startX = size.width / 2 + (i * barWidth * 2) - (barCount * barWidth)

			// Draw bar
			drawLine(
				color = color,
				start = Offset(startX, size.height / 2 - barHeight / 2),
				end = Offset(startX, size.height / 2 + barHeight / 2),
				strokeWidth = barWidth * 0.8f
			)

			// Draw mirrored bar (for symmetry)
			if (i > 0) {
				val mirroredStartX = size.width / 2 - (i * barWidth * 2) + (barCount * barWidth)
				drawLine(
					color = color,
					start = Offset(mirroredStartX, size.height / 2 - barHeight / 2),
					end = Offset(mirroredStartX, size.height / 2 + barHeight / 2),
					strokeWidth = barWidth * 0.8f
				)
			}
		}
	}
}