package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.ui.theme.ColorPaletteRepository
import com.global.sms.ui.theme.CustomThemePalette
import com.global.sms.ui.viewmodels.GlobalSmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCustomizerScreen(
    viewModel: GlobalSmsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedPalette by remember { mutableStateOf(ColorPaletteRepository.palettes.first()) }
    var selectedMode by remember { mutableStateOf("LIGHT") } // LIGHT, DARK, AMOLED

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("شخصی‌سازی پیشرفته پوسته و رنگ‌ها", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("theme_customizer_screen")
        ) {
            // Live SMS Bubble Preview Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = selectedPalette.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "پیش‌نمایش زنده گفتگو",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = selectedPalette.incomingText.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Incoming Bubble
                    Surface(
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp),
                        color = selectedPalette.incomingBubble,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .widthIn(max = 240.dp)
                    ) {
                        Text(
                            text = "سلام! پیامک آزمایشی ورودی با پوسته جدید.",
                            fontSize = 13.sp,
                            color = selectedPalette.incomingText,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Outgoing Bubble
                    Surface(
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp),
                        color = selectedPalette.outgoingBubble,
                        modifier = Modifier
                            .align(Alignment.End)
                            .widthIn(max = 240.dp)
                    ) {
                        Text(
                            text = "عالیه! رنگ حباب و متن کاملاً خانا است.",
                            fontSize = 13.sp,
                            color = selectedPalette.outgoingText,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // Theme Mode Selector
            Text("حالت پوسته (Theme Mode):", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMode == "LIGHT",
                    onClick = {
                        selectedMode = "LIGHT"
                        viewModel.isDarkTheme.value = false
                        viewModel.isAmoledMode.value = false
                    },
                    label = { Text("روشن (Light)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedMode == "DARK",
                    onClick = {
                        selectedMode = "DARK"
                        viewModel.isDarkTheme.value = true
                        viewModel.isAmoledMode.value = false
                    },
                    label = { Text("تیره (Dark)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedMode == "AMOLED",
                    onClick = {
                        selectedMode = "AMOLED"
                        viewModel.isDarkTheme.value = true
                        viewModel.isAmoledMode.value = true
                    },
                    label = { Text("مشکی (AMOLED)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 30+ Predefined Palettes
            Text("پالت‌های رنگی آماده (${ColorPaletteRepository.palettes.size} پالت):", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorPaletteRepository.palettes.forEach { palette ->
                    val isSelected = selectedPalette.id == palette.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPalette = palette
                                if (palette.isDark) {
                                    selectedMode = if (palette.isAmoled) "AMOLED" else "DARK"
                                    viewModel.isDarkTheme.value = true
                                    viewModel.isAmoledMode.value = palette.isAmoled
                                }
                                Toast.makeText(context, "پالت ${palette.name} فعال شد", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = palette.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(palette.primary))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(palette.incomingBubble))
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(palette.outgoingBubble))
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
