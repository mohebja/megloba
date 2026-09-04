package com.global.sms.ui.screens

import com.global.sms.ui.viewmodels.SettingsViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorCustomizationScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()

    var incomingBgColor by remember(settings) { mutableLongStateOf(settings.incomingBubbleBgColor) }
    var incomingTextColor by remember(settings) { mutableLongStateOf(settings.incomingBubbleTextColor) }

    var outgoingBgColor by remember(settings) { mutableLongStateOf(settings.outgoingBubbleBgColor) }
    var outgoingTextColor by remember(settings) { mutableLongStateOf(settings.outgoingBubbleTextColor) }

    var headerColor by remember(settings) { mutableLongStateOf(settings.headerColor) }
    var timestampColor by remember(settings) { mutableLongStateOf(settings.timestampColor) }

    var showSnackbar by remember { mutableStateOf(false) }

    val bgPresetColors = listOf(
        0xFFE9EEF6, // Light Greyish Blue
        0xFF1A73E8, // Primary Blue
        0xFF00658F, // Dark Blue/Teal
        0xFF1E1F2A, // Dark Surface
        0xFF2E3440, // Nord Dark
        0xFFE8F5E9, // Mint
        0xFFFFF3E0, // Warm Peach
        0xFFF3E5F5, // Soft Lavender
        0xFF121212  // AMOLED Black
    )

    val textPresetColors = listOf(
        0xFFFFFFFF, // White
        0xFF1B1F2A, // Dark Navy
        0xFF000000, // Black
        0xFFE9EEF6, // Light Grey
        0xFF1A73E8, // Blue
        0xFF707784  // Grey
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "سفارشی‌سازی رنگ حباب‌ها و پوسته",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Chat Bubble Preview Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(headerColor)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Simulated Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(headerColor).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(headerColor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("A", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "علی محمدی",
                                fontWeight = FontWeight.Bold,
                                color = Color(headerColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Incoming Bubble Preview
                        Column(horizontalAlignment = Alignment.Start) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(incomingBgColor)
                            ) {
                                Text(
                                    "سلام، پیامک جدید دریافت شد. رنگ حباب ورودی!",
                                    color = Color(incomingTextColor),
                                    fontSize = settings.messageTextSizeSp.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Text(
                                "۱۰:۴۰",
                                color = Color(timestampColor),
                                fontSize = settings.dateTextSizeSp.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Outgoing Bubble Preview
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(outgoingBgColor)
                            ) {
                                Text(
                                    "عالیه! این رنگ حباب ارسالی شماست.",
                                    color = Color(outgoingTextColor),
                                    fontSize = settings.messageTextSizeSp.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Text(
                                "۱۰:۴۱",
                                color = Color(timestampColor),
                                fontSize = settings.dateTextSizeSp.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // 1. Incoming Bubble Customization Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "رنگ حباب پیام‌های دریافتی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Text("رنگ پس‌زمینه:", style = MaterialTheme.typography.bodySmall)
                        ColorPickerRow(
                            colors = bgPresetColors,
                            selectedColor = incomingBgColor,
                            onSelect = { incomingBgColor = it }
                        )

                        Text("رنگ متن:", style = MaterialTheme.typography.bodySmall)
                        ColorPickerRow(
                            colors = textPresetColors,
                            selectedColor = incomingTextColor,
                            onSelect = { incomingTextColor = it }
                        )
                    }
                }

                // 2. Outgoing Bubble Customization Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "رنگ حباب پیام‌های ارسالی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Text("رنگ پس‌زمینه:", style = MaterialTheme.typography.bodySmall)
                        ColorPickerRow(
                            colors = bgPresetColors,
                            selectedColor = outgoingBgColor,
                            onSelect = { outgoingBgColor = it }
                        )

                        Text("رنگ متن:", style = MaterialTheme.typography.bodySmall)
                        ColorPickerRow(
                            colors = textPresetColors,
                            selectedColor = outgoingTextColor,
                            onSelect = { outgoingTextColor = it }
                        )
                    }
                }

                // 3. Conversation Header & Timestamp Colors
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "رنگ هدر گفتگو و زمان‌بندی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Text("رنگ شاخص هدر:", style = MaterialTheme.typography.bodySmall)
                        ColorPickerRow(
                            colors = bgPresetColors,
                            selectedColor = headerColor,
                            onSelect = { headerColor = it }
                        )

                        Text("رنگ متن برچسب زمان (Timestamp):", style = MaterialTheme.typography.bodySmall)
                        ColorPickerRow(
                            colors = textPresetColors,
                            selectedColor = timestampColor,
                            onSelect = { timestampColor = it }
                        )
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.resetColorsToDefault()
                            incomingBgColor = 0xFFE9EEF6
                            incomingTextColor = 0xFF1B1F2A
                            outgoingBgColor = 0xFF1A73E8
                            outgoingTextColor = 0xFFFFFFFF
                            headerColor = 0xFF1A73E8
                            timestampColor = 0xFF707784
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بازنشانی پیش‌فرض")
                    }

                    Button(
                        onClick = {
                            viewModel.updateBubbleAndHeaderColors(
                                incomingBg = incomingBgColor,
                                incomingText = incomingTextColor,
                                outgoingBg = outgoingBgColor,
                                outgoingText = outgoingTextColor,
                                headerColor = headerColor,
                                timestampColor = timestampColor
                            )
                            showSnackbar = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_color_settings_button")
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره رنگ‌ها")
                    }
                }

                if (showSnackbar) {
                    Snackbar(
                        dismissAction = { showSnackbar = false }
                    ) {
                        Text("رنگ‌های سفارشی با موفقیت ذخیره شدند.")
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerRow(
    colors: List<Long>,
    selectedColor: Long,
    onSelect: (Long) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors) { colorLong ->
            val isSelected = selectedColor == colorLong
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(colorLong))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onSelect(colorLong) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = if (Color(colorLong).isDarkColor()) Color.White else Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun Color.isDarkColor(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance < 0.5
}
