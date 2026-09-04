package com.global.sms.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Utility for dynamic typography scaling with proportional line height
 * to prevent text line overlap during pinch-to-zoom gestures.
 */
object DynamicTypography {

    fun calculateScaledTextStyle(
        baseFontSizeSp: Float,
        fontScale: Float,
        fontFamily: FontFamily = FontFamily.Default
    ): TextStyle {
        val calculatedFontSize = (baseFontSizeSp * fontScale).coerceIn(10f, 48f)
        val calculatedLineHeight = calculatedFontSize * 1.45f

        return TextStyle(
            fontSize = calculatedFontSize.sp,
            lineHeight = calculatedLineHeight.sp,
            fontFamily = fontFamily
        )
    }

    fun getScaledLineHeight(fontSizeSp: Float, fontScale: Float): TextUnit {
        val calculatedFontSize = (fontSizeSp * fontScale).coerceIn(10f, 48f)
        return (calculatedFontSize * 1.45f).sp
    }
}
