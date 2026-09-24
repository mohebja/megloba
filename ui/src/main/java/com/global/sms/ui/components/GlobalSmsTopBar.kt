package com.global.sms.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.ui.mode.UiMode
import com.global.sms.ui.theme.GlobalSmsDimens

/**
 * Standardized Material 3 Top App Bar for all Global SMS screens.
 *
 * Ensures:
 * 1. Consistent title and optional subtitle typography with Persian support
 * 2. Standard AutoMirrored back navigation icon with TalkBack accessibility
 * 3. Minimum 48dp touch targets on all navigation and action buttons
 * 4. Distinct test tags for automated QA testing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSmsTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onNavigationClick: (() -> Unit)? = null,
    navigationIcon: ImageVector? = Icons.AutoMirrored.Filled.ArrowBack,
    navigationDescription: String = "بازگشت",
    activeMode: UiMode? = null,
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
    ),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        modifier = modifier.testTag("global_top_bar"),
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("top_bar_title")
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("top_bar_subtitle")
                    )
                }
            }
        },
        navigationIcon = {
            if (onNavigationClick != null && navigationIcon != null) {
                IconButton(
                    onClick = onNavigationClick,
                    modifier = Modifier
                        .size(GlobalSmsDimens.touchTarget)
                        .testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = navigationDescription
                    )
                }
            }
        },
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}
