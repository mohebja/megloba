package com.global.sms.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.global.sms.ui.theme.GlobalSmsDimens
import com.global.sms.ui.theme.GlobalSmsShapes

/**
 * Product-grade surface wrapper that automatically adapts to:
 * 1. AMOLED pure black / dark / light color schemes
 * 2. Large screen width constraints (maxContentWidth = 720dp)
 * 3. Consistent shape and border tokens from GlobalSmsDesignTokens
 */
@Composable
fun ProductSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    testTag: String = "product_surface",
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = GlobalSmsDimens.maxContentWidth),
            content = content
        )
    }
}

/**
 * Consistent card component with standard corner radius, padding, and subtle borders.
 */
@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlobalSmsShapes.medium,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    testTag: String = "product_card",
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GlobalSmsDimens.cardPadding),
            content = content
        )
    }
}
