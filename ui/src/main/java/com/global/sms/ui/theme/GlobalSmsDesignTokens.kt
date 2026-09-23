package com.global.sms.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Global visual language for Global SMS.
 *
 * Keeping spacing, corner radii and elevations in one place prevents the
 * Classic, Smart and Enterprise experiences from drifting apart visually.
 */
object GlobalSmsDimens {
    val screenHorizontalPadding = 16.dp
    val compactHorizontalPadding = 12.dp
    val sectionSpacing = 16.dp
    val itemSpacing = 8.dp
    val cardPadding = 12.dp
    val touchTarget = 48.dp
    val avatarSmall = 40.dp
    val avatarMedium = 48.dp
    val avatarLarge = 56.dp
    val maxContentWidth = 720.dp
}

val GlobalSmsShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
