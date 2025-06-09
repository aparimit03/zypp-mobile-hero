package com.example.xyzen.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
	displayLarge = TextStyle(
		fontFamily = poppinsFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 32.sp,
		lineHeight = 40.sp
	),
	displayMedium = TextStyle(
		fontFamily = poppinsFontFamily,
		fontWeight = FontWeight.Bold,
		fontSize = 28.sp,
		lineHeight = 36.sp
	),
	displaySmall = TextStyle(
		fontFamily = poppinsFontFamily,
		fontWeight = FontWeight.SemiBold,
		fontSize = 24.sp,
		lineHeight = 32.sp
	),
	headlineLarge = TextStyle(
		fontFamily = poppinsFontFamily,
		fontWeight = FontWeight.SemiBold,
		fontSize = 22.sp,
		lineHeight = 28.sp
	),
	headlineMedium = TextStyle(
		fontFamily = poppinsFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 20.sp,
		lineHeight = 26.sp
	),
	titleLarge = TextStyle(
		fontFamily = poppinsFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 18.sp,
		lineHeight = 24.sp
	),
	titleMedium = TextStyle(
		fontFamily = poppinsFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 16.sp,
		lineHeight = 22.sp
	),
	bodyLarge = TextStyle(
		fontFamily = poppinsFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp,
		lineHeight = 24.sp
	),
	bodyMedium = TextStyle(
		fontFamily = poppinsFontFamily,
		fontWeight = FontWeight.Normal,
		fontSize = 14.sp,
		lineHeight = 20.sp
	),
	labelLarge = TextStyle(
		fontFamily = poppinsFontFamily,
		fontWeight = FontWeight.Medium,
		fontSize = 14.sp,
		lineHeight = 20.sp
	)
)