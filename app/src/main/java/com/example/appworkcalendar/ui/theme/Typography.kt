package com.example.appworkcalendar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val manropeLike = FontFamily.SansSerif
private val interLike = FontFamily.SansSerif

val GlassTypography = Typography(
    headlineSmall = TextStyle(fontFamily = manropeLike, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleMedium = TextStyle(fontFamily = manropeLike, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = interLike, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = interLike, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = manropeLike, fontWeight = FontWeight.Medium, fontSize = 14.sp)
)
