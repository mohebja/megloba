package com.global.sms.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.ui.mode.UiMode
import com.global.sms.ui.theme.ClassicModeColor
import com.global.sms.ui.theme.EnterpriseModeColor
import com.global.sms.ui.theme.GlobalSmsDimens
import com.global.sms.ui.theme.SmartModeColor

/**
 * Unified Product Mode Header for switching and displaying active modes:
 * - CLASSIC: Minimalist, ultra-fast traditional SMS layout
 * - SMART: AI-powered summaries, smart categorization, OTP extraction
 * - ENTERPRISE: Business campaigns, CRM, customer 360, automation
 */
@Composable
fun ProductModeHeader(
    currentMode: UiMode,
    onModeSelected: (UiMode) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_mode_header"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GlobalSmsDimens.screenHorizontalPadding, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UiMode.values().forEach { mode ->
                    val isSelected = mode == currentMode
                    val modeColor = when (mode) {
                        UiMode.CLASSIC -> ClassicModeColor
                        UiMode.SMART -> SmartModeColor
                        UiMode.ENTERPRISE -> EnterpriseModeColor
                    }
                    val icon: ImageVector = when (mode) {
                        UiMode.CLASSIC -> Icons.AutoMirrored.Filled.Chat
                        UiMode.SMART -> Icons.Default.AutoAwesome
                        UiMode.ENTERPRISE -> Icons.Default.BusinessCenter
                    }

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) modeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        label = "mode_chip_container"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) modeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "mode_chip_content"
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = containerColor,
                        border = if (isSelected) BorderStroke(1.5.dp, modeColor) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .height(GlobalSmsDimens.touchTarget)
                            .clickable { onModeSelected(mode) }
                            .testTag("mode_tab_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = mode.displayName,
                                tint = contentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.displayName,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            if (!compact) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentMode.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
