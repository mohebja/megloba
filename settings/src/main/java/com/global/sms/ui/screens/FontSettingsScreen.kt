package com.global.sms.ui.screens

import com.global.sms.ui.viewmodels.SettingsViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()

    var selectedFontFamily by remember(settings) { mutableStateOf(settings.fontFamily) }
    var messageTextSizeSp by remember(settings) { mutableFloatStateOf(settings.messageTextSizeSp.toFloat()) }
    var senderNameSizeSp by remember(settings) { mutableFloatStateOf(settings.senderNameSizeSp.toFloat()) }
    var dateTextSizeSp by remember(settings) { mutableFloatStateOf(settings.dateTextSizeSp.toFloat()) }

    var showSuccessToast by remember { mutableStateOf(false) }

    val fontFamilies = listOf(
        "Default" to "پیش‌فرض سیستم (Default)",
        "SansSerif" to "بدون پایه (Sans-Serif)",
        "Serif" to "پایه‌دار (Serif)",
        "Monospace" to "فضا ثابت (Monospace)"
    )

    val previewFontFamily = when (selectedFontFamily) {
        "SansSerif" -> FontFamily.SansSerif
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        else -> FontFamily.Default
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "تنظیمات فونت و اندازه متن",
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
                // Live Preview Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "پیش‌نمایش زنده چت",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Incoming message preview
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "فرستنده: بانک ملی",
                                fontSize = senderNameSizeSp.sp,
                                fontFamily = previewFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(settings.incomingBubbleBgColor)
                            ) {
                                Text(
                                    text = "واریز مبلغ ۵,۰۰۰,۰۰۰ ریال به حساب شما با موفقیت انجام شد.",
                                    fontSize = messageTextSizeSp.sp,
                                    fontFamily = previewFontFamily,
                                    color = Color(settings.incomingBubbleTextColor),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Text(
                                text = "۱۰:۴۵ - امروز",
                                fontSize = dateTextSizeSp.sp,
                                fontFamily = previewFontFamily,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Outgoing message preview
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "شما",
                                fontSize = senderNameSizeSp.sp,
                                fontFamily = previewFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(settings.outgoingBubbleBgColor)
                            ) {
                                Text(
                                    text = "ممنون، رسید دریافت شد.",
                                    fontSize = messageTextSizeSp.sp,
                                    fontFamily = previewFontFamily,
                                    color = Color(settings.outgoingBubbleTextColor),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Text(
                                text = "۱۰:۴۶ - امروز",
                                fontSize = dateTextSizeSp.sp,
                                fontFamily = previewFontFamily,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // Font Family Selector
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "خانواده فونت (Font Family)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        fontFamilies.forEach { (key, displayName) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedFontFamily == key,
                                    onClick = { selectedFontFamily = key }
                                )
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Font Sizes Controls
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "اندازه فونت اجزای مختلف",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        // Message text size slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("اندازه متن پیام:")
                                Text("${messageTextSizeSp.toInt()} sp", fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = messageTextSizeSp,
                                onValueChange = { messageTextSizeSp = it },
                                valueRange = 12f..22f,
                                steps = 10,
                                modifier = Modifier.testTag("message_text_size_slider")
                            )
                        }

                        // Sender name size slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("اندازه نام فرستنده:")
                                Text("${senderNameSizeSp.toInt()} sp", fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = senderNameSizeSp,
                                onValueChange = { senderNameSizeSp = it },
                                valueRange = 12f..24f,
                                steps = 12
                            )
                        }

                        // Date text size slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("اندازه تاریخ و زمان:")
                                Text("${dateTextSizeSp.toInt()} sp", fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = dateTextSizeSp,
                                onValueChange = { dateTextSizeSp = it },
                                valueRange = 10f..18f,
                                steps = 8
                            )
                        }
                    }
                }

                // Save Button
                Button(
                    onClick = {
                        viewModel.updateFontSettings(
                            fontFamily = selectedFontFamily,
                            messageTextSizeSp = messageTextSizeSp.toInt(),
                            senderNameSizeSp = senderNameSizeSp.toInt(),
                            dateTextSizeSp = dateTextSizeSp.toInt()
                        )
                        showSuccessToast = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_font_settings_button")
                ) {
                    Icon(Icons.Default.TextFields, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ذخیره تنظیمات فونت")
                }

                if (showSuccessToast) {
                    Snackbar(
                        dismissAction = { showSuccessToast = false }
                    ) {
                        Text("تنظیمات فونت با موفقیت ذخیره شد.")
                    }
                }
            }
        }
    }
}
