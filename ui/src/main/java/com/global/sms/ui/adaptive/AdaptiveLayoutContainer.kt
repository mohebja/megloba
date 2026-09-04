package com.global.sms.ui.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class WindowDisplayMode {
    SINGLE_PANE_PHONE,
    DUAL_PANE_TABLET,
    THREE_PANE_DESKTOP
}

@Composable
fun AdaptiveLayoutContainer(
    modifier: Modifier = Modifier,
    navigationPane: @Composable () -> Unit,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = maxWidth

        val displayMode = when {
            width >= 1000.dp -> WindowDisplayMode.THREE_PANE_DESKTOP
            width >= 600.dp -> WindowDisplayMode.DUAL_PANE_TABLET
            else -> WindowDisplayMode.SINGLE_PANE_PHONE
        }

        when (displayMode) {
            WindowDisplayMode.THREE_PANE_DESKTOP -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        navigationPane()
                    }
                    Box(
                        modifier = Modifier
                            .width(360.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        listPane()
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        detailPane()
                    }
                }
            }

            WindowDisplayMode.DUAL_PANE_TABLET -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        listPane()
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        detailPane()
                    }
                }
            }

            WindowDisplayMode.SINGLE_PANE_PHONE -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    listPane()
                }
            }
        }
    }
}
