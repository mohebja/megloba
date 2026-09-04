package com.global.sms.ui.emoji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSkinTone by remember { mutableStateOf("") } // "", "🏻", "🏼", "🏽", "🏾", "🏿"

    var recentEmojis by remember { mutableStateOf(EmojiRepository.getRecentEmojis(context)) }

    val skinTones = listOf("", "🏻", "🏼", "🏽", "🏾", "🏿")

    val categories = EmojiRepository.categories

    val currentEmojis = remember(selectedCategoryIndex, searchQuery, selectedSkinTone, recentEmojis) {
        if (searchQuery.isNotBlank()) {
            categories.flatMap { it.emojis }.filter { it.contains(searchQuery) }
        } else if (selectedCategoryIndex == 0) {
            recentEmojis
        } else {
            val baseList = categories[selectedCategoryIndex - 1].emojis
            if (selectedSkinTone.isBlank()) {
                baseList
            } else {
                baseList.map { emoji ->
                    if (isSkinToneEligible(emoji)) emoji + selectedSkinTone else emoji
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .testTag("emoji_picker_container"),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Search & Dismiss
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("جستجوی ایموجی...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("emoji_search_input"),
                    shape = RoundedCornerShape(24.dp)
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن ایموجی")
                }
            }

            // Category Scrollable Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                edgePadding = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Recents Tab
                Tab(
                    selected = selectedCategoryIndex == 0,
                    onClick = { selectedCategoryIndex = 0 },
                    text = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "اخیر",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                categories.forEachIndexed { index, cat ->
                    Tab(
                        selected = selectedCategoryIndex == index + 1,
                        onClick = { selectedCategoryIndex = index + 1 },
                        text = {
                            Text(text = cat.icon, fontSize = 18.sp)
                        }
                    )
                }
            }

            // Skin Tone Modifier Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "رنگ پوست:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 6.dp)
                )
                skinTones.forEach { tone ->
                    val isSelected = selectedSkinTone == tone
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable { selectedSkinTone = tone },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👍$tone", fontSize = 12.sp)
                    }
                }
            }

            // Emoji Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(40.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(currentEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable {
                                EmojiRepository.addRecentEmoji(context, emoji)
                                recentEmojis = EmojiRepository.getRecentEmojis(context)
                                onEmojiSelected(emoji)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

private fun isSkinToneEligible(emoji: String): Boolean {
    return emoji in listOf("👍", "👎", "👌", "🤌", "🤏", "✌️", "🤞", "🤟", "🤘", "🤙", "👈", "👉", "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "👏", "🙌", "👐", "🤲", "🙏", "💪")
}
